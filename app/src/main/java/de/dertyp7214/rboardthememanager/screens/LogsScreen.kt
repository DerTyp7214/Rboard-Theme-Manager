package de.dertyp7214.rboardthememanager.screens

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.dertyp7214.logs.helpers.DogbinUtils
import com.dertyp7214.logs.helpers.Logger
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.rboardthememanager.Config.MODULE_ID
import de.dertyp7214.rboardthememanager.Config.MODULE_PATH
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.copyRecursively
import de.dertyp7214.rboardthememanager.core.tar
import de.dertyp7214.rboardthememanager.utils.MagiskUtils
import kotlinx.android.synthetic.main.activity_logs_screen.*
import java.io.File
import java.util.*

class LogsScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs_screen)
        setSupportActionBar(toolbar)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_logs, menu)
        menu.findItem(R.id.action_share_magisk_module).isVisible = MagiskUtils.isMagiskInstalled()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear -> {
                Logger.clear()
                Toast.makeText(this, R.string.cleared, Toast.LENGTH_LONG).show()
                finish()
                true
            }
            R.id.action_share -> {
                DogbinUtils.upload(
                    Logger.logsToMessage(this),
                    object : DogbinUtils.UploadResultCallback {
                        override fun onFail(message: String, e: Exception) {
                            Toast.makeText(this@LogsScreen, R.string.share_error, Toast.LENGTH_LONG)
                                .show()
                        }

                        override fun onSuccess(url: String) {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, url)
                                type = "text/plain"
                            }
                            startActivity(
                                Intent.createChooser(
                                    sendIntent,
                                    getString(R.string.send_to)
                                )
                            )
                        }
                    })
                true
            }
            R.id.action_share_magisk_module -> {
                val tar = File(cacheDir, "$MODULE_ID.tar")
                val tmpModule = SuFile(cacheDir, MODULE_ID)
                tar.delete()
                SuFile(MODULE_PATH).copyRecursively(tmpModule)
                tmpModule.tar(tar)
                tmpModule.deleteRecursive()
                val uri = FileProvider.getUriForFile(
                    this,
                    packageName,
                    tar
                )
                ShareCompat.IntentBuilder(this)
                    .setStream(uri)
                    .setType("*/*")
                    .intent
                    .setAction(Intent.ACTION_SEND)
                    .setDataAndType(uri, "*/*")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).apply {
                        startActivity(
                            Intent.createChooser(
                                this,
                                getString(R.string.share_module)
                            )
                        )
                    }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
