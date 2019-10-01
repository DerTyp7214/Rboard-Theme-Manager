package de.dertyp7214.rboardthememanager.screens

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.MenuBottomSheet
import de.dertyp7214.rboardthememanager.data.MenuItem
import de.dertyp7214.rboardthememanager.enum.GridLayout
import de.dertyp7214.rboardthememanager.viewmodels.HomeViewModel
import kotlinx.android.synthetic.main.bottom_navigation.*

class HomeActivity : AppCompatActivity() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var bottomSheet: MenuBottomSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val dark = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val light = dark or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val isDark = resources.getBoolean(R.bool.darkmode)

        window.decorView.systemUiVisibility = if (isDark) dark else light

        homeViewModel = ViewModelProviders.of(this)[HomeViewModel::class.java]
        homeViewModel.loadFromStorage(this)

        menuButton.setOnClickListener {
            bottomSheet = MenuBottomSheet(arrayListOf(
                MenuItem(
                    R.drawable.ic_list,
                    R.string.list_grid,
                    homeViewModel.getGridLayout() == GridLayout.SINGLE
                ) {
                    homeViewModel.setGridLayout(GridLayout.SINGLE, this)
                    bottomSheet.dismiss()
                },
                MenuItem(
                    R.drawable.grid,
                    R.string.grid_small,
                    homeViewModel.getGridLayout() == GridLayout.SMALL
                ) {
                    homeViewModel.setGridLayout(GridLayout.SMALL, this)
                    bottomSheet.dismiss()
                },
                MenuItem(
                    R.drawable.grid,
                    R.string.grid_big,
                    homeViewModel.getGridLayout() == GridLayout.BIG
                ) {
                    homeViewModel.setGridLayout(GridLayout.BIG, this)
                    bottomSheet.dismiss()
                }
            ))
            bottomSheet.show(supportFragmentManager, "")
        }
    }
}
