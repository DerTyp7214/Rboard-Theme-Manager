package de.dertyp7214.rboardthememanager.helper

import android.content.Context
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.material.button.MaterialButton
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.MaterialBottomSheet
import de.dertyp7214.rboardthememanager.data.PackItem
import java.io.File


class DownloadHelper {

    private var from = ""
    private var to = ""
    private var fileName = ""
    private var listener: DownloadListener = object : DownloadListener {
        override fun start() {}
        override fun progress(progress: Int, current: Long, total: Long) {}
        override fun error(error: String) {}
        override fun end(path: String) {}
    }

    fun from(url: String): DownloadHelper {
        from = url
        return this
    }

    fun to(path: String): DownloadHelper {
        to = path
        return this
    }

    fun fileName(name: String): DownloadHelper {
        fileName = name
        return this
    }

    fun setListener(l: DownloadListener): DownloadHelper {
        listener = l
        return this
    }

    fun start() {
        PRDownloader.download(from, to, fileName)
            .build()
            .setOnStartOrResumeListener { listener.start() }
            .setOnProgressListener {
                val progress = (it.currentBytes.toFloat() / it.totalBytes.toFloat()) * 100F
                listener.progress(progress.toInt(), it.currentBytes, it.totalBytes)
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    listener.end(File(to, fileName).absolutePath)
                }

                override fun onError(error: Error?) {
                    listener.error(error?.connectionException?.localizedMessage ?: "")
                }
            })
    }
}

fun previewDialog(
    activity: FragmentActivity,
    previewPath: String,
    themePack: PackItem,
    clickDownload: (closeDialog: () -> Unit) -> Unit = { it() },
    onShow: (Pair<ProgressBar, MaterialBottomSheet>) -> Unit
) {
    MaterialBottomSheet().show(activity.supportFragmentManager, R.layout.preview) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val title = findViewById<TextView>(R.id.dialog_title)
        val author = findViewById<TextView>(R.id.author)
        title.text = themePack.name
        author.text = themePack.author

        onShow(Pair(progressBar, this))

        val downloadButton: MaterialButton = findViewById(R.id.download_button)

        downloadButton.isEnabled = false

        downloadButton.setOnClickListener {
            clickDownload { dismiss() }
        }

        setOnCancelListener {
            File(previewPath).deleteRecursively()
        }
    }
}

fun downloadDialog(context: Context): Pair<ProgressBar, MaterialDialog> {
    lateinit var progressBar: ProgressBar
    val dialog = MaterialDialog(context).show {
        setContentView(R.layout.download_alert)
        cancelable(false)
        cancelOnTouchOutside(false)
        progressBar = findViewById(R.id.progressbar_downloading)
        val imageView = findViewById<ImageView>(R.id.dialog_image_view)
        imageView.viewTreeObserver.addOnGlobalLayoutListener {
            progressBar.scaleY = imageView.measuredHeight.toFloat()
        }
    }
    return Pair(progressBar, dialog)
}

interface DownloadListener {
    fun start()
    fun progress(progress: Int, current: Long, total: Long)
    fun error(error: String)
    fun end(path: String)
}