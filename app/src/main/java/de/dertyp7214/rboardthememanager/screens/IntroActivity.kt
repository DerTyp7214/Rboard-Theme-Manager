package de.dertyp7214.rboardthememanager.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.fragments.SelectRuntimeData
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.intro_navigator.*

class IntroActivity : AppCompatActivity() {

    var index: Int = 0
    var selectRuntimeData = SelectRuntimeData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        indicator.createIndicators(3, index)

        floatingActionButton.setOnClickListener {
            index++
            openPage()
        }
    }

    private fun openPage() {
        val controller = fragment.findNavController()
        val name = resources.getResourceEntryName(controller.currentDestination?.id ?: 0)
        when (index) {
            0 -> {
                controller.navigate(R.id.action_selectRuntimeFragment_to_welcomeFragment)
            }
            1 -> {
                if (name == "welcomeFragment") controller.navigate(R.id.action_welcomeFragment_to_selectRuntimeFragment)
                else controller.navigate(R.id.action_permissionsFragment_to_selectRuntimeFragment)
            }
            2 -> {
                if (name == "selectRuntimeFragment") controller.navigate(R.id.action_selectRuntimeFragment_to_permissionsFragment)
                else {
                    index--
                    openPage()
                }
            }
        }
        if (index < 3) {
            indicator.animatePageSelected(index)
        } else {
            index = 3
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (index != 0) {
            index--
            openPage()
        }
    }
}
