package de.dertyp7214.rboardthememanager.screens

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.getFileName
import de.dertyp7214.rboardthememanager.core.writeToFile
import de.dertyp7214.rboardthememanager.helper.ThemeHelper
import de.dertyp7214.rboardthememanager.utils.FileUtils.getThemePacksPath
import java.io.File

class ImportPack : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_pack)

        intent.data?.let {
            val zip = File(
                getThemePacksPath(this),
                it.getFileName(this)
            ).apply { delete(); it.writeToFile(this@ImportPack, this) }
            if (zip.exists() && ThemeHelper.installTheme(zip, false)) Toast.makeText(
                this,
                R.string.theme_added,
                Toast.LENGTH_SHORT
            ).show()
            else Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
            true
        } ?: Toast.makeText(this, R.string.noFile, Toast.LENGTH_LONG).show()

        finishAndRemoveTask()
    }
}
