package de.dertyp7214.rboardthememanager.fragments

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.rboardthememanager.Config
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.copyRecursively
import de.dertyp7214.rboardthememanager.core.dpToPx
import de.dertyp7214.rboardthememanager.core.forEach
import de.dertyp7214.rboardthememanager.core.safeParse
import de.dertyp7214.rboardthememanager.data.PackItem
import de.dertyp7214.rboardthememanager.helper.*
import de.dertyp7214.rboardthememanager.utils.FileUtils.getSoundPacksPath
import de.dertyp7214.rboardthememanager.utils.SoundUtils
import de.dertyp7214.rboardthememanager.viewmodels.SoundsViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class SoundsFragment : Fragment() {

    private lateinit var adapter: Adapter
    private lateinit var soundsViewModel: SoundsViewModel
    private lateinit var refreshLayout: SwipeRefreshLayout

    private val list = ArrayList<PackItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_sounds, container, false)

        val activity = requireActivity()
        val context = requireContext()

        refreshLayout = v.findViewById(R.id.refreshLayout)
        soundsViewModel =
            activity.run { ViewModelProvider(this)[SoundsViewModel::class.java] }

        adapter = Adapter(requireActivity(), list) {
            soundsViewModel.setRefetch(true)
            Toast.makeText(
                context,
                "Sounds installed, reboot for changes to take effect.",
                Toast.LENGTH_SHORT
            ).show()
        }

        refreshLayout.setProgressViewOffset(
            true,
            0,
            5.dpToPx(context).toInt()
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
            StartOffsetItemDecoration(0)
        )
        recyclerView.addItemDecoration(
            EndOffsetItemDecoration(56.dpToPx(context).toInt())
        )

        fetchDownloadList()

        return v
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchDownloadList() {
        list.removeAll(list)
        Thread {
            val json = JSONArray().safeParse(URL(Config.SOUNDS_PACKS_URL).readText(Charsets.UTF_8))
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

            try {
                list.sortBy { it.name.toLowerCase(Locale.getDefault()) }
            } catch (e: Exception) {
                Logger.log(Logger.Companion.Type.ERROR, "FetchDownload", e.localizedMessage)
            }
            activity?.runOnUiThread {
                adapter.notifyDataSetChanged()
                refreshLayout.isRefreshing = false
            }
        }.start()
    }

    class Adapter(
        private val context: FragmentActivity,
        private val list: ArrayList<PackItem>,
        private val callback: () -> Unit
    ) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        val previewsPath: String = File(getSoundPacksPath(context), "previews").absolutePath

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.pack_item,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return list.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pack = list[position]

            holder.title.text = pack.name
            holder.author.text = pack.author
            holder.image.setImageResource(R.drawable.ic_sounds)
            holder.image.setColorFilter(context.getColor(R.color.colorAccent))

            holder.layout.setOnClickListener {
                previewDialog(context, previewsPath, pack, {
                    val pair = downloadDialog(context).apply {
                        first.isIndeterminate = false
                    }
                    DownloadHelper()
                        .from(pack.url)
                        .to(getSoundPacksPath(context).absolutePath)
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

                                getSoundsDirectory()?.path?.let { soundsPath ->
                                    Logger.log(
                                        Logger.Companion.Type.INFO,
                                        "DOWNLOAD_ZIP",
                                        "from: $path to ${Config.MODULE_PATH}$soundsPath/audio/ui"
                                    )

                                    val tmpPath =
                                        SuFile(getSoundPacksPath(context), "tmp").absolutePath

                                    if (ZipHelper().unpackZip(
                                            tmpPath,
                                            path
                                        ) && SuFile(tmpPath).exists()
                                    ) {
                                        SuFile(tmpPath).copyRecursively(SuFile("${Config.MODULE_PATH}$soundsPath/audio/ui"))
                                    } else Toast.makeText(
                                        context,
                                        R.string.error,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    it()
                                }

                                pair.second.dismiss()
                                callback()
                            }
                        }).start()
                }) { pair ->
                    DownloadHelper()
                        .from(pack.url)
                        .to(getSoundPacksPath(context).absolutePath)
                        .fileName("preview_temp.zip").setListener(
                            object : DownloadListener {
                                override fun start() {
                                    SuFile(previewsPath).deleteRecursively()
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
                                        SoundPreviewAdapter(
                                            SoundUtils.loadPreviewSounds(context)
                                        )

                                    pair.second.findViewById<MaterialButton>(R.id.download_button)?.isEnabled =
                                        true

                                    val recyclerView =
                                        pair.second.findViewById<RecyclerView>(R.id.preview_recyclerview)
                                    recyclerView?.layoutManager = LinearLayoutManager(context)
                                    recyclerView?.setHasFixedSize(true)
                                    recyclerView?.adapter = adapter
                                    recyclerView?.addItemDecoration(
                                        StartOffsetItemDecoration(
                                            0
                                        )
                                    )

                                    recyclerView?.visibility = View.VISIBLE
                                    pair.first.visibility = View.GONE

                                    val bDialog = pair.second.dialog
                                    if (bDialog is BottomSheetDialog) {
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            bDialog.behavior.state =
                                                BottomSheetBehavior.STATE_EXPANDED
                                        }, 100)
                                    }
                                }
                            }
                        ).start()
                }
            }
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val layout: ViewGroup = v.findViewById(R.id.root)
            val title: TextView = v.findViewById(R.id.title)
            val author: TextView = v.findViewById(R.id.author)
            val image: ImageView = v.findViewById(R.id.image)
        }
    }
}

private class SoundPreviewAdapter(
    private val list: List<File>
) : RecyclerView.Adapter<SoundPreviewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.pack_item,
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
        val file = list[position]

        holder.title.text = file.name.removeSuffix(".ogg")
        holder.author.text = file.name

        holder.layout.setOnClickListener {
            MediaPlayer().apply {
                try {
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        release()
                    }
                } catch (e: Exception) {
                    release()
                }
            }
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val layout: ViewGroup = v.findViewById(R.id.root)
        val title: TextView = v.findViewById(R.id.title)
        val author: TextView = v.findViewById(R.id.author)
    }
}
