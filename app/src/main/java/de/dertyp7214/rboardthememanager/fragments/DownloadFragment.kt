package de.dertyp7214.rboardthememanager.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertyp7214.logs.helpers.Logger
import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration
import com.jaredrummler.android.shell.Shell
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.rboardthememanager.Config.MAGISK_THEME_LOC
import de.dertyp7214.rboardthememanager.Config.PACKS_URL
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.*
import de.dertyp7214.rboardthememanager.data.PackItem
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.helper.*
import de.dertyp7214.rboardthememanager.utils.ColorUtils
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_download, container, false)

        refreshLayout = v.findViewById(R.id.refreshLayout)
        homeViewModel = requireActivity().run {
            ViewModelProviders.of(this)[HomeViewModel::class.java]
        }

        adapter = Adapter(requireContext(), list) {
            homeViewModel.setRefetch(true)
            Toast.makeText(context, R.string.downloaded, Toast.LENGTH_SHORT).show()
        }

        refreshLayout.setProgressViewOffset(
            true,
            0,
            requireContext().getStatusBarHeight() + 5.dpToPx(requireContext()).toInt()
        )

        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorPrimaryLight)
        refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.primaryText)
        refreshLayout.setOnRefreshListener {
            fetchDownloadList()
        }

        val recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            StartOffsetItemDecoration(requireContext().getStatusBarHeight())
        )
        recyclerView.addItemDecoration(
            EndOffsetItemDecoration(56.dpToPx(requireContext()).toInt())
        )

        fetchDownloadList()

        return v
    }

    private fun fetchDownloadList() {
        list.removeAll(list)
        Thread {
            val json = JSONArray().safeParse(URL(PACKS_URL).readText(UTF_8))
            json.forEach { o, _ ->
                if (o is JSONObject && o.has("author") && o.has("url") && o.has("title"))
                    list.add(
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

            list.sortBy { it.name.toLowerCase(Locale.getDefault()) }

            activity?.runOnUiThread {
                adapter.notifyDataSetChanged()
                refreshLayout.isRefreshing = false
            }
        }.start()
    }

    class Adapter(
        private val context: Context,
        private val list: ArrayList<PackItem>,
        private val callback: () -> Unit
    ) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        var previewsPath = File(
            context.getExternalFilesDirs(Environment.DIRECTORY_NOTIFICATIONS)
                ?.get(0)?.absolutePath?.removeSuffix(
                    "Notifications"
                ), "ThemePacks"
        ).absolutePath + "/previews"

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(context).inflate(
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

            holder.layout.setOnLongClickListener {

                if (!Shell.SU.available()) {
                    return@setOnLongClickListener false
                }

                val pair = previewDialog(context, previewsPath, pack.name)

                DownloadHelper().from(pack.url).to(
                    File(
                        context.getExternalFilesDirs(Environment.DIRECTORY_NOTIFICATIONS)[0].absolutePath.removeSuffix(
                            "Notifications"
                        ), "ThemePacks"
                    ).absolutePath
                )
                    .fileName(
                        "preview_temp.zip"
                    ).setListener(
                        object : DownloadListener {
                            override fun start() {
                                File(previewsPath).deleteRecursively()
                            }

                            override fun progress(progress: Int, current: Long, total: Long) {
                            }

                            override fun error(error: String) {
                                Log.d("ERROR", error)
                            }

                            override fun end(path: String) {
                                pair.first.isIndeterminate = true
                                SuFile("$path/previews").mkdirs()
                                val folderPath = File(
                                    context.getExternalFilesDirs(Environment.DIRECTORY_NOTIFICATIONS)[0].absolutePath.removeSuffix(
                                        "Notifications"
                                    ), "ThemePacks"
                                ).absolutePath

                                ZipHelper().unpackZip("$folderPath/previews", path)

                                val adapter =
                                    PreviewAdapter(context, ThemeUtils.loadPreviewThemes(context))

                                val recyclerView =
                                    pair.second.findViewById<RecyclerView>(R.id.preview_recyclerview)
                                recyclerView.layoutManager = LinearLayoutManager(context)
                                recyclerView.setHasFixedSize(true)
                                recyclerView.adapter = adapter
                                recyclerView.addItemDecoration(
                                    StartOffsetItemDecoration(
                                        (context.getStatusBarHeight())
                                    )
                                )

                                recyclerView.visibility = View.VISIBLE
                                pair.first.visibility = View.GONE

                            }
                        }
                    ).start()

                return@setOnLongClickListener true
            }

            holder.layout.setOnClickListener {

                if (!Shell.SU.available()) {
                    return@setOnClickListener
                }

                val pair = downloadDialog(context).apply {
                    first.isIndeterminate = false
                }
                DownloadHelper()
                    .from(pack.url)
                    .to(
                        File(
                            context.getExternalFilesDirs(Environment.DIRECTORY_NOTIFICATIONS)[0].absolutePath.removeSuffix(
                                "Notifications"
                            ), "ThemePacks"
                        ).absolutePath
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
                                "from: $MAGISK_THEME_LOC to $path"
                            )
                            ZipHelper().unpackZip(MAGISK_THEME_LOC, path)
                            pair.second.dismiss()
                            callback()
                        }
                    })
                    .start()
            }
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val layout: ViewGroup = v.findViewById(R.id.root)
            val title: TextView = v.findViewById(R.id.title)
            val author: TextView = v.findViewById(R.id.author)
        }
    }
}

class PreviewAdapter(
    private val context: Context,
    private val list: List<ThemeDataClass>
) : RecyclerView.Adapter<HomeGridFragment.GridThemeAdapter.ViewHolder>() {

    @SuppressLint("UseCompatLoadingForDrawables")
    private val default = context.resources.getDrawable(
        R.drawable.ic_keyboard,
        null
    ).getBitmap()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeGridFragment.GridThemeAdapter.ViewHolder {
        return HomeGridFragment.GridThemeAdapter.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.theme_grid_item_single,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(
        holder: HomeGridFragment.GridThemeAdapter.ViewHolder,
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
            dataClass.name.split("_").joinToString(" ") { it.capitalize() }
        holder.themeNameSelect.text =
            dataClass.name.split("_").joinToString(" ") { it.capitalize() }

        holder.themeName.setTextColor(if (ColorUtils.isColorLight(color)) Color.BLACK else Color.WHITE)

        if (dataClass.selected)
            holder.selectOverlay.alpha = 1F
        else
            holder.selectOverlay.alpha = 0F

        holder.card.setCardBackgroundColor(color)
    }

}
