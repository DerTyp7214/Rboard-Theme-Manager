package de.dertyp7214.rboardthememanager.screens

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dertyp7214.logs.helpers.DogbinUtils
import com.dertyp7214.logs.helpers.Logger
import de.dertyp7214.rboardthememanager.R
import kotlinx.android.synthetic.main.activity_logs_screen.*
import java.text.SimpleDateFormat
import java.util.*

class LogsScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs_screen)
        setSupportActionBar(toolbar)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_logs, menu)
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
                DogbinUtils.upload(getSharedPreferences("logs", MODE_PRIVATE).all.map {
                    "${SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.ROOT).format(Date(it.key.toLong()))}: ${it.value}"
                }.joinToString("\n"), object : DogbinUtils.UploadResultCallback {
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
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.send_to)))
                    }
                })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
