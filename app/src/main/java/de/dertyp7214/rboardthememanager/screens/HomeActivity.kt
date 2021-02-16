@file:Suppress("DEPRECATION")

package de.dertyp7214.rboardthememanager.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.lifecycle.ViewModelProvider
import com.dertyp7214.preferencesplus.core.dp
import com.dertyp7214.preferencesplus.core.setHeight
import com.dertyp7214.preferencesplus.core.setMargins
import com.dertyp7214.preferencesplus.core.setWidth
import com.github.zawadz88.materialpopupmenu.popupMenu
import de.dertyp7214.rboardthememanager.BuildConfig
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.InputBottomSheet
import de.dertyp7214.rboardthememanager.component.MenuBottomSheet
import de.dertyp7214.rboardthememanager.core.delayed
import de.dertyp7214.rboardthememanager.core.getBitmap
import de.dertyp7214.rboardthememanager.data.Filter
import de.dertyp7214.rboardthememanager.data.MenuItem
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.enums.GridLayout
import de.dertyp7214.rboardthememanager.fragments.DownloadFragment
import de.dertyp7214.rboardthememanager.fragments.HomeGridFragment
import de.dertyp7214.rboardthememanager.fragments.SoundsFragment
import de.dertyp7214.rboardthememanager.keyboardheight.KeyboardHeightObserver
import de.dertyp7214.rboardthememanager.keyboardheight.KeyboardHeightProvider
import de.dertyp7214.rboardthememanager.utils.ColorUtils
import de.dertyp7214.rboardthememanager.utils.ThemeUtils
import de.dertyp7214.rboardthememanager.viewmodels.HomeViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.bottom_navigation.*
import java.util.*

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
    private var currentFragment = R.id.navigation_themes

    @SuppressLint("deprecation")
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
            val dark = View.SYSTEM_UI_FLAG_VISIBLE
            val light = dark or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

            window.decorView.systemUiVisibility = if (isDark) dark else light
        }

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeViewModel.loadFromStorage(this)

        supportFragmentManager.beginTransaction().apply {
            replace(fragment.id, HomeGridFragment())
            commit()
        }

        homeNav.setOnNavigationItemSelectedListener {
            navigate(it.itemId, true)
            true
        }

        searchButton.setOnClickListener { _ ->
            inputBottomSheet =
                InputBottomSheet(
                    if (currentFragment == R.id.navigation_themes) homeViewModel.getFilter() else homeViewModel.getFilterDownloads().value,
                    { _, keyCode, _ ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) inputBottomSheet?.dismiss()
                        true
                    },
                    { text, it ->
                        when (currentFragment) {
                            R.id.navigation_themes -> homeViewModel.setFilter(text.toString())
                            R.id.navigation_downloads -> homeViewModel.setFilterDownloads {
                                Filter(text.toString(), it.tags)
                            }
                        }
                        it.dismiss()
                    }) { input, _ ->
                    val popupMenu = popupMenu {
                        style = R.style.PopupMenu
                        if (currentFragment == R.id.navigation_themes) section {
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
                        else section {
                            item {
                                label = "Comming Soon"
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
            }, "",
                getThemeView(ThemeUtils.getActiveTheme())
            )
            bottomSheet?.show(supportFragmentManager, "")
        }
    }

    @SuppressLint("InflateParams")
    private fun getThemeView(theme: ThemeDataClass): View {
        return LinearLayout(this).apply {
            orientation = VERTICAL
            setMargins(8.dp(this@HomeActivity), 0, 8.dp(this@HomeActivity), 0)
            addView(layoutInflater.inflate(R.layout.single_theme_item, null).apply {
                val themeName = findViewById<TextView>(R.id.theme_name)
                val themeIcon = findViewById<ImageView>(R.id.theme_image)
                val card: CardView = findViewById(R.id.card)
                val gradient: View? = try {
                    findViewById(R.id.gradient)
                } catch (e: Exception) {
                    null
                }

                val defaultImage = ContextCompat.getDrawable(
                    this@HomeActivity,
                    R.drawable.ic_keyboard
                )!!.getBitmap()

                themeIcon.setImageBitmap(theme.image ?: defaultImage)
                themeIcon.colorFilter = theme.colorFilter

                val color = ColorUtils.dominantColor(themeIcon.drawable.getBitmap())
                val isDark = ColorUtils.isColorLight(color)

                if (gradient != null) {
                    val g = GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(color, Color.TRANSPARENT)
                    )
                    gradient.background = g
                }

                card.setCardBackgroundColor(color)

                themeName.text =
                    theme.name.split("_").joinToString(" ") { it.capitalize(Locale.getDefault()) }
                themeName.setTextColor(if (!isDark) Color.WHITE else Color.BLACK)
            })
            addView(View(this@HomeActivity).apply {
                setHeight(8.dp(this@HomeActivity))
            })
            addView(View(this@HomeActivity).apply {
                setHeight(1.dp(this@HomeActivity))
                setWidth(LinearLayout.LayoutParams.MATCH_PARENT)
                setBackgroundColor(getColor(R.color.primaryTextSec))
            })
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

    fun navigate(id: Int, self: Boolean = false) {
        if (currentFragment != id || currentFragment == R.id.navigation_themes) {
            currentFragment = id
            if (!self) homeNav.selectedItemId = id
            when (id) {
                R.id.navigation_themes -> supportFragmentManager.beginTransaction().apply {
                    replace(fragment.id, HomeGridFragment())
                    setTransition(TRANSIT_FRAGMENT_OPEN)
                    commit()
                    if (self) delayed(100) { homeViewModel.setRefetch(true) }
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
    }
}
