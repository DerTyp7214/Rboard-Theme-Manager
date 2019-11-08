package de.dertyp7214.rboardthememanager.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertyp7214.logs.helpers.Logger
import com.dgreenhalgh.android.simpleitemdecoration.grid.GridBottomOffsetItemDecoration
import com.dgreenhalgh.android.simpleitemdecoration.grid.GridTopOffsetItemDecoration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.SelectedThemeBottomSheet
import de.dertyp7214.rboardthememanager.core.*
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.enums.GridLayout
import de.dertyp7214.rboardthememanager.helper.ThemeHelper
import de.dertyp7214.rboardthememanager.helper.TimeLogger
import de.dertyp7214.rboardthememanager.utils.ColorUtils.dominantColor
import de.dertyp7214.rboardthememanager.utils.ColorUtils.isColorLight
import de.dertyp7214.rboardthememanager.utils.ThemeUtils.loadThemes
import de.dertyp7214.rboardthememanager.viewmodels.HomeViewModel
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class HomeGridFragment : Fragment() {

    private val tmpList = ArrayList<ThemeDataClass>()
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var recyclerView: RecyclerView
    private val themeList = ArrayList<ThemeDataClass>()

    private val addTheme = 187

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_home_grid, container, false)

        val fabAdd = v.findViewById<FloatingActionButton>(R.id.fabAdd)
        refreshLayout = v.findViewById(R.id.refreshLayout)
        recyclerView = v.findViewById(R.id.theme_list)
        homeViewModel = activity!!.run {
            ViewModelProviders.of(this)[HomeViewModel::class.java]
        }

        refreshLayout.setProgressViewOffset(
            true,
            0,
            context!!.getStatusBarHeight() + 5.dpToPx(context!!).toInt()
        )
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorPrimaryLight)
        refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.primaryText)
        refreshLayout.setOnRefreshListener {
            homeViewModel.setRefetch(true)
        }

        refreshLayout.isRefreshing = true
        fabAdd.setMargin(bottomMargin = 68.dpToPx(context!!).toInt())
        fabAdd.setOnClickListener {
            val intent = Intent()
                .setType("application/zip")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select a theme"), addTheme)
        }

        val adapter = GridThemeAdapter(activity!!, themeList, homeViewModel)

        homeViewModel.gridLayoutObserve(this, Observer {
            recyclerView.stopScroll()
            adapter.notifyDataSetChanged()
            if (recyclerView.layoutManager is GridLayoutManager) {
                val columns = if (it == GridLayout.SINGLE) 1 else 2
                (recyclerView.layoutManager as GridLayoutManager).spanCount = columns
                for (i in 0 until recyclerView.itemDecorationCount) {
                    recyclerView.removeItemDecorationAt(0)
                }
                recyclerView.addItemDecoration(
                    GridTopOffsetItemDecoration(
                        context!!.getStatusBarHeight(),
                        columns
                    )
                )
                recyclerView.addItemDecoration(
                    GridBottomOffsetItemDecoration(
                        56.dpToPx(context!!).toInt(),
                        columns
                    )
                )
            }
        })

        homeViewModel.observeRefetch(this, Observer {
            if (it) {
                homeViewModel.setFilter(homeViewModel.getFilter())
            }
        })

        homeViewModel.gridLayoutObserve(this, Observer {
            recyclerView.stopScroll()
            adapter.notifyDataSetChanged()
        })

        homeViewModel.themesObserve(this, Observer { list ->
            if (tmpList.isEmpty() || list.size > tmpList.size) tmpList.apply {
                clear(adapter)
                addAll(list)
                forEach {
                    Logger.log(
                        Logger.Companion.Type.INFO,
                        "LOAD THEMES",
                        "${it.selected} | ${it.name} | ${it.path}"
                    )
                }
            }
        })

        homeViewModel.observeFilter(this, Observer { filter ->
            refreshLayout.isRefreshing = true
            ObjectAnimator.ofFloat(recyclerView, "alpha", 0F).apply {
                duration = 300
                startDelay = 200
                start()
            }
            Thread {
                themeList.clear(adapter)
                themeList.addAll((if (!homeViewModel.getRefetch()) tmpList else loadThemes()).filter {
                    filter.isBlank() || it.name.contains(
                        filter,
                        true
                    )
                }.sortedBy {
                    it.name.toLowerCase(
                        Locale.ROOT
                    )
                })
                activity?.runOnUiThread {
                    homeViewModel.setThemes(themeList)
                    recyclerView.stopScroll()
                    adapter.notifyDataSetChanged()
                    refreshLayout.isRefreshing = false
                    if (homeViewModel.getRefetch()) homeViewModel.setRefetch(false)
                    ObjectAnimator.ofFloat(recyclerView, "alpha", 1F).apply {
                        duration = 300
                        startDelay = 200
                        start()
                    }
                }
            }.start()
        })

        context!!.delayed(200) {
            if (!homeViewModel.themesExist()) {
                Thread {
                    loadThemes().apply {
                        themeList.clear(adapter)
                        themeList.addAll(sortedBy { it.name.toLowerCase(Locale.ROOT) })
                    }
                    activity?.runOnUiThread {
                        homeViewModel.setThemes(themeList)
                        recyclerView.stopScroll()
                        adapter.notifyDataSetChanged()
                        refreshLayout.isRefreshing = false
                        ObjectAnimator.ofFloat(recyclerView, "alpha", 1F).apply {
                            duration = 300
                            startDelay = 200
                            start()
                        }
                    }
                }.start()
            } else {
                themeList.clear(adapter)
                themeList.addAll(homeViewModel.getThemes())
                recyclerView.stopScroll()
                adapter.notifyDataSetChanged()
                refreshLayout.isRefreshing = false
                ObjectAnimator.ofFloat(recyclerView, "alpha", 1F).apply {
                    duration = 300
                    startDelay = 200
                    start()
                }
            }
        }

        val columns = if (homeViewModel.getGridLayout() == GridLayout.SINGLE) 1 else 2

        val layoutManager = GridLayoutManager(context, columns)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(200)
        recyclerView.addItemDecoration(
            GridTopOffsetItemDecoration(
                context!!.getStatusBarHeight(),
                columns
            )
        )
        recyclerView.addItemDecoration(
            GridBottomOffsetItemDecoration(
                56.dpToPx(context!!).toInt(),
                columns
            )
        )

        homeViewModel.getRecyclerViewState().apply {
            if (this != null) {
                recyclerView.layoutManager?.onRestoreInstanceState(this)
            }
        }

        return v
    }

    @SuppressLint("InflateParams", "SdCardPath")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == addTheme && resultCode == RESULT_OK && data != null && data.data != null) {
            val zip = File("/storage/emulated/0/${getRealPathFromURI(data.data!!).split(":").last()}")
            if (ThemeHelper.installTheme(zip)) Toast.makeText(context, R.string.theme_added, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDetach() {
        super.onDetach()
        homeViewModel.setRecyclerViewState(recyclerView.layoutManager?.onSaveInstanceState())
    }

    class GridThemeAdapter(
        private val context: FragmentActivity,
        private val list: List<ThemeDataClass>,
        private val homeViewModel: HomeViewModel
    ) :
        RecyclerView.Adapter<GridThemeAdapter.ViewHolder>() {

        private var recyclerView: RecyclerView? = null

        private var activeTheme = ""
        private val default = context.resources.getDrawable(
            R.drawable.ic_keyboard,
            null
        ).getBitmap()

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.recyclerView = recyclerView
            activeTheme = ThemeHelper.getActiveTheme()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    when (viewType) {
                        0 -> R.layout.theme_grid_item_single
                        1 -> R.layout.theme_grid_item_small
                        else -> R.layout.theme_grid_item
                    },
                    parent,
                    false
                )
            )
        }

        override fun getItemViewType(position: Int): Int {
            return when (homeViewModel.getGridLayout()) {
                GridLayout.SINGLE -> 0
                GridLayout.SMALL -> 1
                GridLayout.BIG -> 2
            }
        }

        override fun getItemCount(): Int = list.size

        @SuppressLint("SetTextI18n", "DefaultLocale")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val timingLogger = TimeLogger("HomeGridFragment", "OnBind Adapter", false)
            timingLogger.reset()
            val selection = list.map { it.selected }.contains(true)
            val dataClass = list[position]

            timingLogger.addSplit("Init")

            timingLogger.addSplit("Image")

            val color = dominantColor(dataClass.image ?: default)

            timingLogger.addSplit("Color")

            if (holder.gradient != null) {
                val gradient = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(color, Color.TRANSPARENT)
                )
                holder.gradient.background = gradient
            }

            timingLogger.addSplit("Gradient")

            holder.themeImage.setImageBitmap(dataClass.image ?: default)
            holder.themeImage.alpha = if (dataClass.image != null) 1F else .3F

            timingLogger.addSplit("Apply Image")

            holder.themeName.text =
                "${dataClass.name.split("_").joinToString(" ") { it.capitalize() }} ${if (dataClass.name == activeTheme) "(applied)" else ""}"
            holder.themeNameSelect.text =
                "${dataClass.name.split("_").joinToString(" ") { it.capitalize() }} ${if (dataClass.name == activeTheme) "(applied)" else ""}"

            timingLogger.addSplit("Titles")

            holder.themeName.setTextColor(if (isColorLight(color)) Color.BLACK else Color.WHITE)

            timingLogger.addSplit("Text Color")

            if (dataClass.selected)
                holder.selectOverlay.alpha = 1F
            else
                holder.selectOverlay.alpha = 0F

            timingLogger.addSplit("Selection Overlay")

            holder.card.setCardBackgroundColor(color)

            timingLogger.addSplit("Card Background")

            holder.card.setOnClickListener {
                if (selection) {
                    list[position].selected = !list[position].selected
                    ObjectAnimator.ofFloat(
                        holder.selectOverlay,
                        "alpha",
                        1F - holder.selectOverlay.alpha
                    ).apply {
                        duration = 100
                        start()
                    }.doOnEnd {
                        recyclerView?.stopScroll()
                        notifyDataSetChanged()
                    }
                } else {
                    SelectedThemeBottomSheet(dataClass, default, color, isColorLight(color)) {
                        homeViewModel.setRefetch(true)
                    }.show(
                        context.supportFragmentManager,
                        ""
                    )
                }
            }

            timingLogger.addSplit("Click Listener")

            holder.card.setOnLongClickListener {
                list[position].selected = true
                ObjectAnimator.ofFloat(
                    holder.selectOverlay,
                    "alpha",
                    1F
                ).apply {
                    duration = 100
                    start()
                }.doOnEnd {
                    recyclerView?.stopScroll()
                    notifyDataSetChanged()
                }
                true
            }

            timingLogger.addSplit("Longclick Listener")

            timingLogger.dumpToLog()
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val themeImage: ImageView = v.findViewById(R.id.theme_image)
            val themeName: TextView = v.findViewById(R.id.theme_name)
            val themeNameSelect: TextView = v.findViewById(R.id.theme_name_selected)
            val selectOverlay: ViewGroup = v.findViewById(R.id.select_overlay)
            val card: CardView = v.findViewById(R.id.card)
            val gradient: View? = try {
                v.findViewById(R.id.gradient)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getRealPathFromURI(uri: Uri): String {
        var filePath = uri.path
        if (filePath?.startsWith("/storage") == true)
            return filePath

        val wholeID = DocumentsContract.getDocumentId(uri)

        val id = wholeID.split(":")[1]

        val column = arrayOf(MediaStore.Files.FileColumns.DATA)

        val sel = MediaStore.Files.FileColumns.DATA + " LIKE '%" + id + "%'"

        val cursor = context!!.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            column, sel, null, null
        )

        val columnIndex = cursor!!.getColumnIndex(column[0])

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex)
        }
        cursor.close()
        return filePath!!
    }
}
