package de.dertyp7214.rboardthememanager.screens

import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
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
            ).apply { it.writeToFile(this@ImportPack, this) }
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

    private fun getRealPathFromURI(uri: Uri): String {
        var filePath = uri.path
        if (filePath?.startsWith("/storage") == true)
            return filePath

        val wholeID = DocumentsContract.getDocumentId(uri)

        val id = wholeID.split(":")[1]

        val column = arrayOf(MediaStore.Files.FileColumns.DATA)

        val sel = MediaStore.Files.FileColumns.DATA + " LIKE '%" + id + "%'"

        val cursor = contentResolver?.query(
            MediaStore.Files.getContentUri("external"),
            column, sel, null, null
        )

        val columnIndex = cursor?.getColumnIndex(column[0])

        if (cursor?.moveToFirst() == true && columnIndex != null) {
            filePath = cursor.getString(columnIndex)
        }
        cursor?.close()
        return filePath!!
    }
}
