package de.dertyp7214.rboardthememanager.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertyp7214.logs.helpers.Logger
import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.rboardthememanager.Config.MAGISK_THEME_LOC
import de.dertyp7214.rboardthememanager.Config.PACKS_URL
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.*
import de.dertyp7214.rboardthememanager.data.PackItem
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.helper.*
import de.dertyp7214.rboardthememanager.screens.HomeActivity
import de.dertyp7214.rboardthememanager.utils.ColorUtils
import de.dertyp7214.rboardthememanager.utils.FileUtils.getThemePacksPath
import de.dertyp7214.rboardthememanager.utils.ThemeUtils
import de.dertyp7214.rboardthememanager.viewmodels.HomeViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.text.Charsets.UTF_8

class DownloadFragment : Fragment() {

    private lateinit var adapter: Adapter
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var refreshLayout: SwipeRefreshLayout

    private val list = ArrayList<PackItem>()
    private val tmpList = ArrayList<PackItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_download, container, false)

        refreshLayout = v.findViewById(R.id.refreshLayout)
        homeViewModel = requireActivity().run {
            ViewModelProviders.of(this)[HomeViewModel::class.java]
        }

        adapter = Adapter(requireActivity(), list)

        refreshLayout.setProgressViewOffset(
            true,
            0,
            5.dpToPx(requireContext()).toInt()
        )

        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorPrimaryLight)
        refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.primaryText)
        refreshLayout.setOnRefreshListener {
            homeViewModel.setRefetchDownloads(true)
        }

        val recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            StartOffsetItemDecoration(0)
        )
        recyclerView.addItemDecoration(
            EndOffsetItemDecoration(56.dpToPx(requireContext()).toInt())
        )

        homeViewModel.observeFilterDownloads(this) { filter ->
            refreshLayout.isRefreshing = true
            fun filter() {
                list.clear(adapter)
                list.addAll(tmpList.filter {
                    filter.isBlank() || it.name.contains(
                        filter,
                        true
                    ) || it.author.contains(
                        filter,
                        true
                    )
                }.sortedBy {
                    it.name.toLowerCase(
                        Locale.getDefault()
                    )
                })
                refreshLayout.isRefreshing = false
            }
            if (homeViewModel.getRefetchDownloads()) fetchDownloadList { filter() } else filter()
        }

        homeViewModel.observeRefetchDownloads(this) {
            if (it) {
                homeViewModel.setFilterDownloads(homeViewModel.getFilterDownloads())
            }
        }

        fetchDownloadList()

        return v
    }

    private fun fetchDownloadList(callback: () -> Unit = {}) {
        list.removeAll(list)
        tmpList.clear()
        Thread {
            val json = JSONArray().safeParse(URL(PACKS_URL).readText(UTF_8))
            json.forEach { o, _ ->
                if (o is JSONObject && o.has("author") && o.has("url") && o.has("title"))
                    tmpList.add(
                        PackItem(
                            o.getString("title"),
                            o.getString("author"),
                            o.getString("url")
                        ).apply {
                            Logger.log(
                                Logger.Companion.Type.INFO,
                                "DOWNLOAD ITEM",
                                "$name | $author | $url"
                            )
                        }
                    )
            }

            tmpList.sortBy { it.name.toLowerCase(Locale.getDefault()) }
            list.addAll(tmpList)

            activity?.runOnUiThread {
                adapter.notifyDataSetChanged()
                refreshLayout.isRefreshing = false
                callback()
            }
        }.start()
    }

    class Adapter(
        private val activity: Activity,
        private val list: ArrayList<PackItem>
    ) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        val previewsPath: String = File(getThemePacksPath(activity), "previews").absolutePath

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(activity).inflate(
                    R.layout.pack_item,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int = list.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pack = list[position]

            holder.title.text = pack.name
            holder.author.text = "by ${pack.author}"

            holder.layout.setOnClickListener {
                val pair = previewDialog(activity, previewsPath, pack.name) {
                    downloadThemePack(pack) {
                        it()
                        if (activity is HomeActivity) activity.navigate(R.id.navigation_themes)
                    }
                }

                DownloadHelper().from(pack.url).to(
                    getThemePacksPath(activity).absolutePath
                )
                    .fileName(
                        "preview_temp.zip"
                    ).setListener(
                        object : DownloadListener {
                            override fun start() {
                                SuFile(previewsPath).deleteRecursive()
                            }

                            override fun progress(progress: Int, current: Long, total: Long) {
                            }

                            override fun error(error: String) {
                                Log.d("ERROR", error)
                            }

                            override fun end(path: String) {
                                pair.first.isIndeterminate = true
                                SuFile(previewsPath).mkdirs()

                                ZipHelper().unpackZip(previewsPath, path)

                                val adapter =
                                    PreviewAdapter(
                                        activity,
                                        ArrayList(ThemeUtils.loadPreviewThemes(activity))
                                    )

                                val recyclerView =
                                    pair.second.findViewById<RecyclerView>(R.id.preview_recyclerview)
                                recyclerView.layoutManager = LinearLayoutManager(activity)
                                recyclerView.setHasFixedSize(true)
                                recyclerView.adapter = adapter
                                recyclerView.addItemDecoration(
                                    StartOffsetItemDecoration(
                                        0
                                    )
                                )

                                recyclerView.visibility = View.VISIBLE
                                pair.first.visibility = View.GONE
                            }
                        }
                    ).start()
            }
        }

        private fun downloadThemePack(pack: PackItem, callback: () -> Unit) {
            val pair = downloadDialog(activity).apply {
                first.isIndeterminate = false
            }
            DownloadHelper()
                .from(pack.url)
                .to(
                    getThemePacksPath(activity).absolutePath
                )
                .fileName("tmp.zip")
                .setListener(object : DownloadListener {
                    override fun start() {
                        Log.d("START", "START")
                    }

                    override fun progress(progress: Int, current: Long, total: Long) {
                        pair.first.progress = progress
                    }

                    override fun error(error: String) {
                        Logger.log(
                            Logger.Companion.Type.ERROR,
                            "DOWNLOAD",
                            "${pack.name} $error"
                        )
                    }

                    override fun end(path: String) {
                        pair.first.isIndeterminate = true
                        Logger.log(
                            Logger.Companion.Type.INFO,
                            "DOWNLOAD_ZIP",
                            "from: $path to $MAGISK_THEME_LOC"
                        )
                        ZipHelper().unpackZip(MAGISK_THEME_LOC, path)
                        pair.second.dismiss()
                        callback()
                    }
                })
                .start()
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val layout: ViewGroup = v.findViewById(R.id.root)
            val title: TextView = v.findViewById(R.id.title)
            val author: TextView = v.findViewById(R.id.author)
        }
    }
}

private class PreviewAdapter(
    private val context: Context,
    private val list: List<ThemeDataClass>
) : RecyclerView.Adapter<PreviewAdapter.ViewHolder>() {

    @SuppressLint("UseCompatLoadingForDrawables")
    private val default = context.resources.getDrawable(
        R.drawable.ic_keyboard,
        null
    ).getBitmap()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.theme_grid_item_single,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val dataClass = list[position]
        val color = ColorUtils.dominantColor(dataClass.image ?: default)
        if (holder.gradient != null) {
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(color, Color.TRANSPARENT)
            )
            holder.gradient.background = gradient
        }

        holder.themeImage.setImageBitmap(dataClass.image ?: default)
        holder.themeImage.alpha = if (dataClass.image != null) 1F else .3F

        holder.themeName.text =
            dataClass.name.split("_").joinToString(" ") { it.capitalize(Locale.getDefault()) }
        holder.themeNameSelect.text =
            dataClass.name.split("_").joinToString(" ") { it.capitalize(Locale.getDefault()) }

        holder.themeName.setTextColor(if (ColorUtils.isColorLight(color)) Color.BLACK else Color.WHITE)

        if (dataClass.selected)
            holder.selectOverlay.alpha = 1F
        else
            holder.selectOverlay.alpha = 0F

        holder.card.setCardBackgroundColor(color)

        holder.card.setOnClickListener {
            val success = ThemeHelper.installTheme(SuFile(dataClass.path))
                    && if (dataClass.image != null) ThemeHelper.installTheme(
                SuFile(
                    dataClass.path.removeSuffix(
                        ".zip"
                    )
                )
            )
            else true
            Logger.log(
                Logger.Companion.Type.DEBUG,
                "INSTALL THEME",
                "${dataClass.name}: $success | Image: ${dataClass.image != null}"
            )
            if (success) Toast.makeText(context, R.string.theme_added, Toast.LENGTH_LONG).show()
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val themeImage: ImageView = v.findViewById(R.id.theme_image)
        val themeName: TextView = v.findViewById(R.id.theme_name)
        val themeNameSelect: TextView = v.findViewById(R.id.theme_name_selected)
        val selectOverlay: ViewGroup = v.findViewById(R.id.select_overlay)
        val card: CardView = v.findViewById(R.id.card)
        val gradient: View? = try {
            v.findViewById(R.id.gradient)
        } catch (e: Exception) {
            null
        }
    }
}
