package de.dertyp7214.rboardthememanager.screens

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.lifecycle.ViewModelProviders
import com.github.zawadz88.materialpopupmenu.popupMenu
import de.dertyp7214.rboardthememanager.BuildConfig
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.InputBottomSheet
import de.dertyp7214.rboardthememanager.component.MenuBottomSheet
import de.dertyp7214.rboardthememanager.data.MenuItem
import de.dertyp7214.rboardthememanager.enums.GridLayout
import de.dertyp7214.rboardthememanager.fragments.DownloadFragment
import de.dertyp7214.rboardthememanager.fragments.HomeGridFragment
import de.dertyp7214.rboardthememanager.fragments.SoundsFragment
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
    private var bottomSheet: MenuBottomSheet? = null
    private var inputBottomSheet: InputBottomSheet? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var currentFragment = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        keyboardHeightProvider = KeyboardHeightProvider(this)

        homeNav.post {
            keyboardHeightProvider?.start()
        }

        val isDark = resources.getBoolean(R.bool.darkmode)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                if (isDark) 0 else WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            val dark = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            val light = dark or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

            window.decorView.systemUiVisibility = if (isDark) dark else light
        }

        homeViewModel = ViewModelProviders.of(this)[HomeViewModel::class.java]
        homeViewModel.loadFromStorage(this)

        supportFragmentManager.beginTransaction().apply {
            replace(fragment.id, HomeGridFragment())
            commit()
        }

        homeNav.setOnNavigationItemSelectedListener {
            if (currentFragment != it.itemId || currentFragment == R.id.navigation_themes) {
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
                    R.id.navigation_sounds -> supportFragmentManager.beginTransaction().apply {
                        replace(fragment.id, SoundsFragment())
                        setTransition(TRANSIT_FRAGMENT_OPEN)
                        commit()
                    }
                }
            }
            true
        }

        searchButton.setOnClickListener { _ ->
            inputBottomSheet =
                InputBottomSheet(homeViewModel.getFilter(), { _, keyCode, _ ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) inputBottomSheet?.dismiss()
                    true
                }, { text, it ->
                    homeViewModel.setFilter(text.toString())
                    it.dismiss()
                }) { input, _ ->
                    val popupMenu = popupMenu {
                        style = R.style.PopupMenu
                        section {
                            item {
                                labelRes = R.string.list_grid
                                icon = R.drawable.ic_list
                                callback = {
                                    homeViewModel.setGridLayout(
                                        GridLayout.SINGLE,
                                        this@HomeActivity
                                    )
                                    inputBottomSheet?.dismiss()
                                }
                            }
                            item {
                                labelRes = R.string.grid_small
                                icon = R.drawable.grid
                                callback = {
                                    homeViewModel.setGridLayout(GridLayout.SMALL, this@HomeActivity)
                                    inputBottomSheet?.dismiss()
                                }
                            }
                            item {
                                labelRes = R.string.grid_big
                                icon = R.drawable.grid
                                callback = {
                                    homeViewModel.setGridLayout(GridLayout.BIG, this@HomeActivity)
                                    inputBottomSheet?.dismiss()
                                }
                            }
                        }
                    }
                    popupMenu.show(this, input)
                }.setKeyBoardHeightObserver(this, homeViewModel)
            inputBottomSheet?.show(supportFragmentManager, "")
        }

        menuButton.setOnClickListener {
            bottomSheet = MenuBottomSheet(arrayListOf(
                MenuItem(
                    R.drawable.data,
                    R.string.data,
                    false
                ) {
                    startActivity(Intent(this, InfoScreen::class.java))
                    bottomSheet?.dismiss()
                },
                MenuItem(
                    R.drawable.about,
                    R.string.about,
                    false
                ) {
                    startActivity(Intent(this, AboutActivity::class.java))
                    bottomSheet?.dismiss()
                },
                MenuItem(
                    R.drawable.settings,
                    R.string.settings,
                    false
                ) {
                    startActivity(Intent(this, Settings::class.java))
                    bottomSheet?.dismiss()
                },
                MenuItem(
                    R.drawable.ic_flag_24px,
                    R.string.flags, false
                ) {
                    startActivity(Intent(this, FlagsActivity::class.java))
                    bottomSheet?.dismiss()
                }
            ).apply {
                if (BuildConfig.DEBUG) {
                    add(MenuItem(
                        R.drawable.logs,
                        R.string.logs,
                        false
                    ) {
                        startActivity(Intent(this@HomeActivity, LogsScreen::class.java))
                        bottomSheet?.dismiss()
                    })
                }
            }, ""
            )
            bottomSheet?.show(supportFragmentManager, "")
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
