package de.dertyp7214.rboardthememanager.fragments

import android.annotation.SuppressLint
import android.content.Context
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
import com.dertyp7214.logs.helpers.Logger
import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration
import de.dertyp7214.rboardthememanager.Config.MAGISK_THEME_LOC
import de.dertyp7214.rboardthememanager.Config.PACKS_URL
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.dpToPx
import de.dertyp7214.rboardthememanager.core.forEach
import de.dertyp7214.rboardthememanager.core.getStatusBarHeight
import de.dertyp7214.rboardthememanager.core.safeParse
import de.dertyp7214.rboardthememanager.data.PackItem
import de.dertyp7214.rboardthememanager.helper.DownloadHelper
import de.dertyp7214.rboardthememanager.helper.DownloadListener
import de.dertyp7214.rboardthememanager.helper.ZipHelper
import de.dertyp7214.rboardthememanager.helper.downloadDialog
import de.dertyp7214.rboardthememanager.viewmodels.HomeViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL
import kotlin.text.Charsets.UTF_8

class DownloadFragment : Fragment() {

    private lateinit var adapter: Adapter
    private lateinit var homeViewModel: HomeViewModel

    private val list = ArrayList<PackItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_download, container, false)

        homeViewModel = activity!!.run {
            ViewModelProviders.of(this)[HomeViewModel::class.java]
        }

        adapter = Adapter(context!!, list) {
            homeViewModel.setRefetch(true)
            Toast.makeText(context, R.string.downloaded, Toast.LENGTH_SHORT).show()
        }

        val recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            StartOffsetItemDecoration(context!!.getStatusBarHeight())
        )
        recyclerView.addItemDecoration(
            EndOffsetItemDecoration(56.dpToPx(context!!).toInt())
        )

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
            activity?.runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }.start()

        return v
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

        override fun getItemCount(): Int = list.size

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
                            Logger.log(Logger.Companion.Type.ERROR, "DOWNLOAD", "${pack.name} $error")
                        }

                        override fun end(path: String) {
                            pair.first.isIndeterminate = true
                            Logger.log(Logger.Companion.Type.INFO, "DOWNLOAD_ZIP", "from: $MAGISK_THEME_LOC to $path")
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
