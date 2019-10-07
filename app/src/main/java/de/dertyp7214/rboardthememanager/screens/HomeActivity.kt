package de.dertyp7214.rboardthememanager.screens

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.lifecycle.ViewModelProviders
import com.github.zawadz88.materialpopupmenu.popupMenu
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.InputBottomSheet
import de.dertyp7214.rboardthememanager.component.MenuBottomSheet
import de.dertyp7214.rboardthememanager.core.hideKeyboard
import de.dertyp7214.rboardthememanager.data.MenuItem
import de.dertyp7214.rboardthememanager.enums.GridLayout
import de.dertyp7214.rboardthememanager.fragments.DownloadFragment
import de.dertyp7214.rboardthememanager.fragments.HomeGridFragment
import de.dertyp7214.rboardthememanager.keyboardheight.KeyboardHeightObserver
import de.dertyp7214.rboardthememanager.keyboardheight.KeyboardHeightProvider
import de.dertyp7214.rboardthememanager.viewmodels.HomeViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.bottom_navigation.*


class HomeActivity : AppCompatActivity(), KeyboardHeightObserver {

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        try {
            homeViewModel.setKeyboardHeight(height)
        } catch (e: Exception) {
        }
    }

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var bottomSheet: MenuBottomSheet
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var currentFragment = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        keyboardHeightProvider = KeyboardHeightProvider(this)

        homeNav.post {
            keyboardHeightProvider?.start()
        }

        val dark = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val light = dark or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val isDark = resources.getBoolean(R.bool.darkmode)

        window.decorView.systemUiVisibility = if (isDark) dark else light

        homeViewModel = ViewModelProviders.of(this)[HomeViewModel::class.java]
        homeViewModel.loadFromStorage(this)

        supportFragmentManager.beginTransaction().apply {
            replace(fragment.id, HomeGridFragment())
            commit()
        }

        homeNav.setOnNavigationItemSelectedListener {
            if (currentFragment != it.itemId) {
                currentFragment = it.itemId
                when (it.itemId) {
                    R.id.navigation_themes -> supportFragmentManager.beginTransaction().apply {
                        replace(fragment.id, HomeGridFragment())
                        setTransition(TRANSIT_FRAGMENT_OPEN)
                        commit()
                    }
                    R.id.navigation_downloads -> supportFragmentManager.beginTransaction().apply {
                        replace(fragment.id, DownloadFragment())
                        setTransition(TRANSIT_FRAGMENT_OPEN)
                        commit()
                    }
                }
            }
            true
        }

        searchButton.setOnClickListener { _ ->
            val bottomSheet = InputBottomSheet(View.OnKeyListener { _, _, _ ->
                true
            }, { text, it ->
                Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                it.dismiss()
            }) { input, _ ->
                val popupMenu = popupMenu {
                    style = R.style.PopupMenu
                    section {
                        item {
                            label = "Test"
                            icon = R.drawable.ic_list
                            callback = {
                                Toast.makeText(this@HomeActivity, "TEST", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                popupMenu.show(this, input)
                hideKeyboard()
            }.setKeyBoardHeightObserver(this, homeViewModel)
            bottomSheet.show(supportFragmentManager, "")
        }

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

    override fun onResume() {
        super.onResume()
        keyboardHeightProvider?.setKeyboardHeightObserver(this)
    }

    override fun onPause() {
        super.onPause()
        keyboardHeightProvider?.setKeyboardHeightObserver(object : KeyboardHeightObserver {
            override fun onKeyboardHeightChanged(height: Int, orientation: Int) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHeightProvider?.close()
    }
}
