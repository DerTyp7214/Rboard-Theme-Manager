package de.dertyp7214.rboardthememanager.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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
import de.dertyp7214.rboardthememanager.Config
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.dpToPx
import de.dertyp7214.rboardthememanager.core.forEach
import de.dertyp7214.rboardthememanager.core.safeParse
import de.dertyp7214.rboardthememanager.data.PackItem
import de.dertyp7214.rboardthememanager.helper.*
import de.dertyp7214.rboardthememanager.utils.FileUtils.getSoundPacksPath
import de.dertyp7214.rboardthememanager.viewmodels.SoundsViewModel
import org.json.JSONArray
import org.json.JSONObject
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
            activity.run { ViewModelProviders.of(this)[SoundsViewModel::class.java] }

        adapter = Adapter(context, list) {
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
            holder.author.text = "by ${pack.author}"

            holder.layout.setOnClickListener {
                val pair = downloadDialog(context).apply {
                    first.isIndeterminate = false
                }
                DownloadHelper()
                    .from(pack.url)
                    .to(
                        getSoundPacksPath(context).absolutePath
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


                            ThemeHelper.getSoundsDirectory()?.path?.let { soundsPath ->
                                Logger.log(
                                    Logger.Companion.Type.INFO,
                                    "DOWNLOAD_ZIP",
                                    "from: ${Config.MODULE_PATH}$soundsPath/audio/ui to $path"
                                )

                                ZipHelper().unpackZip(
                                    "${Config.MODULE_PATH}$soundsPath/audio/ui",
                                    path
                                )
                            }

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
