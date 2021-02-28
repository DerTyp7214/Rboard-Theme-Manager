package de.dertyp7214.rboardthememanager.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertyp7214.logs.helpers.Logger
import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.rboardthememanager.BuildConfig
import de.dertyp7214.rboardthememanager.Config.PACKS_URL
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.*
import de.dertyp7214.rboardthememanager.data.Filter
import de.dertyp7214.rboardthememanager.data.PackItem
import de.dertyp7214.rboardthememanager.helper.*
import de.dertyp7214.rboardthememanager.screens.HomeActivity
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
    private lateinit var tagsGroup: ChipGroup
    private lateinit var clearTags: Chip

    private val list = ArrayList<PackItem>()
    private val tmpList = ArrayList<PackItem>()

    private var fetching: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_download, container, false)

        tagsGroup = v.findViewById(R.id.tags)
        refreshLayout = v.findViewById(R.id.refreshLayout)
        homeViewModel = requireActivity().run {
            ViewModelProvider(this)[HomeViewModel::class.java]
        }

        clearTags = v.findViewById(R.id.clear_tags)

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

        if (homeViewModel.getFilterDownloads().tags.any { it.second }) clearTags.visibility =
            VISIBLE
        else clearTags.visibility = GONE

        clearTags.setOnClickListener {
            homeViewModel.setFilterDownloads {
                Filter(it.value, it.tags.map { tag -> Pair(tag.first, false) })
            }
            refreshChips(homeViewModel.getFilterDownloads().tags, requireActivity())
        }

        homeViewModel.observeFilterDownloads(this) { filter ->
            refreshLayout.isRefreshing = true
            fun filter() {
                list.clear(adapter)
                list.addAll(tmpList.filter {
                    (filter.value.isBlank() || it.name.contains(
                        filter.value,
                        true
                    ) || it.author.contains(
                        filter.value,
                        true
                    )) && (filter.tags.filter { tag -> tag.second }.map { tag -> tag.first }
                        .containsAny(it.tags) || !filter.tags.map { tag -> tag.second }
                        .contains(true))
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
        if (!fetching) {
            tmpList.clear()
            Thread {
                fetching = true
                val json = JSONArray().safeParse(URL(PACKS_URL).readText(UTF_8))
                json.forEach { o, _ ->
                    if (o is JSONObject && o.has("author") && o.has("url") && o.has("title"))
                        tmpList.add(
                            PackItem(
                                o.getString("title"),
                                o.getString("author"),
                                o.getString("url"),
                                o.getList("tags")
                            ).apply {
                                Logger.log(
                                    Logger.Companion.Type.INFO,
                                    "DOWNLOAD ITEM",
                                    "$name | $author | $url"
                                )
                            }
                        )
                }

                try {
                    tmpList.sortBy { it.name.toLowerCase(Locale.getDefault()) }
                } catch (e: Exception) {
                    Logger.log(Logger.Companion.Type.ERROR, "FetchDownload", e.localizedMessage)
                }
                list.removeAll(list)
                list.addAll(tmpList)

                activity?.runOnUiThread {
                    homeViewModel.setFilterDownloads {
                        val tags = ArrayList<Pair<String, Boolean>>()
                        tmpList.forEach { item ->
                            tags.addAll(item.tags.map { tag -> Pair(tag, false) })
                        }
                        if (BuildConfig.DEBUG) for (i in 0..20) tags.add(Pair("YEET-$i", false))
                        refreshChips(tags, requireActivity())
                        Filter(it.value, tags)
                    }

                    adapter.notifyDataSetChanged()
                    refreshLayout.isRefreshing = false
                    homeViewModel.setRefetchDownloads(false)
                    callback()
                }
                fetching = false
            }.start()
        }
    }

    private fun refreshChips(tags: List<Pair<String, Boolean>>, activity: Activity) {
        tagsGroup.removeAllViews()
        tags.forEach { tag ->
            val defaultColor = activity.getColor(R.color.primaryTextSec)
            val accentColor = activity.getColor(R.color.colorAccent)
            val accentTransparentColor =
                activity.getColor(R.color.colorAccentTransparent)

            val chip = Chip(activity)
            chip.rippleColor =
                ColorStateList.valueOf(if (tag.second) accentColor else defaultColor)
            chip.chipBackgroundColor =
                ColorStateList.valueOf(if (tag.second) accentTransparentColor.changeAlpha(0x30) else Color.TRANSPARENT)
            chip.chipStrokeColor =
                ColorStateList.valueOf(if (tag.second) accentColor else defaultColor)
            chip.chipStrokeWidth = 1.dpToPx(activity)
            chip.text = tag.first
            chip.setOnClickListener {
                toggleTag(tag.first, !tag.second)
                refreshChips(homeViewModel.getFilterDownloads().tags, activity)
            }

            if (tags.any { it.second }) clearTags.visibility =
                VISIBLE
            else clearTags.visibility = GONE

            tagsGroup.addView(chip)

        }
    }

    private fun toggleTag(tag: String, value: Boolean) {
        homeViewModel.setFilterDownloads {
            val tags = ArrayList<Pair<String, Boolean>>()
            it.tags.forEach { item ->
                tags.add(Pair(item.first, if (item.first == tag) value else item.second))
            }
            Filter(it.value, tags)
        }
    }

    class Adapter(
        private val activity: FragmentActivity,
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
            holder.author.text = pack.author
            holder.image.setImageResource(R.drawable.ic_style)
            holder.image.setColorFilter(activity.getColor(R.color.colorAccent))

            holder.layout.setOnClickListener {
                previewDialog(activity, previewsPath, pack, {
                    downloadThemePack(pack) {
                        it()
                        if (activity is HomeActivity) activity.navigate(R.id.navigation_themes)
                    }
                }) { pair ->
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

                                    try {
                                        val adapter =
                                            PreviewAdapter(
                                                activity,
                                                ArrayList(ThemeUtils.loadPreviewThemes(activity))
                                            )

                                        pair.second.findViewById<MaterialButton>(R.id.download_button)?.isEnabled =
                                            true

                                        val recyclerView =
                                            pair.second.findViewById<RecyclerView>(R.id.preview_recyclerview)
                                        recyclerView?.layoutManager = LinearLayoutManager(activity)
                                        recyclerView?.setHasFixedSize(true)
                                        recyclerView?.adapter = adapter
                                        recyclerView?.addItemDecoration(
                                            StartOffsetItemDecoration(
                                                0
                                            )
                                        )

                                        recyclerView?.visibility = VISIBLE
                                        pair.first.visibility = GONE

                                        val bDialog = pair.second.dialog
                                        if (bDialog is BottomSheetDialog) {
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                bDialog.behavior.state =
                                                    BottomSheetBehavior.STATE_EXPANDED
                                            }, 100)
                                        }
                                    } catch (e: Exception) {
                                        Logger.log(
                                            Logger.Companion.Type.ERROR,
                                            "Previews",
                                            e.localizedMessage
                                        )
                                    }
                                }
                            }
                        ).start()
                }
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
                        val cacheDir = SuFile(getThemePacksPath(activity), "tmp_themes")
                        Logger.log(
                            Logger.Companion.Type.INFO,
                            "DOWNLOAD_ZIP",
                            "from: $path to $cacheDir"
                        )
                        ZipHelper().unpackZip(cacheDir.absolutePath, path)
                        cacheDir.listFiles()?.forEach {
                            ThemeHelper.installTheme(it)
                        }
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
            val image: ImageView = v.findViewById(R.id.image)
        }
    }
}