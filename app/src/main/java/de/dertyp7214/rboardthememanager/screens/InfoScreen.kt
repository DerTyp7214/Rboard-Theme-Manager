package de.dertyp7214.rboardthememanager.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.fragments.InfoFragment
import kotlinx.android.synthetic.main.activity_info_screen.*

class InfoScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_screen)

        supportFragmentManager.beginTransaction().apply {
            replace(fragment.id, InfoFragment())
            commit()
        }
    }
}
