package de.dertyp7214.rboardthememanager.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.dertyp7214.logs.helpers.Logger
import com.dertyp7214.preferencesplus.core.setMargins
import com.dgreenhalgh.android.simpleitemdecoration.grid.GridBottomOffsetItemDecoration
import com.dgreenhalgh.android.simpleitemdecoration.grid.GridTopOffsetItemDecoration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.SelectedThemeBottomSheet
import de.dertyp7214.rboardthememanager.core.*
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.enums.GridLayout
import de.dertyp7214.rboardthememanager.helper.SwipeToDeleteCallback
import de.dertyp7214.rboardthememanager.helper.ThemeHelper
import de.dertyp7214.rboardthememanager.helper.TimeLogger
import de.dertyp7214.rboardthememanager.utils.ColorUtils.dominantColor
import de.dertyp7214.rboardthememanager.utils.ColorUtils.isColorLight
import de.dertyp7214.rboardthememanager.utils.FileUtils
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
    private lateinit var toolbar: Toolbar
    private val themeList = ArrayList<ThemeDataClass>()

    private val addTheme = 187

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_home_grid, container, false)

        toolbar = requireActivity().findViewById(R.id.select_toolbar)

        val fabAdd = v.findViewById<FloatingActionButton>(R.id.fabAdd)
        refreshLayout = v.findViewById(R.id.refreshLayout)
        recyclerView = v.findViewById(R.id.theme_list)
        homeViewModel = requireActivity().run {
            ViewModelProviders.of(this)[HomeViewModel::class.java]
        }

        val keyboardImg = v.findViewById<ImageView>(R.id.imageView5)
        keyboardImg.alpha = 1F

        refreshLayout.setProgressViewOffset(
            true,
            0,
            requireContext().getStatusBarHeight() + 5.dpToPx(requireContext()).toInt()
        )
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorPrimaryLight)
        refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.primaryText)
        refreshLayout.setOnRefreshListener {
            homeViewModel.setRefetch(true)
            toggleToolbar(false)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                    fabAdd.hide()
                else if (dy < 0)
                    fabAdd.show()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == SCROLL_STATE_IDLE) {
                    fabAdd.show();
                }
            }
        })

        refreshLayout.isRefreshing = true
        fabAdd.setMargin(bottomMargin = 68.dpToPx(requireContext()).toInt())
        fabAdd.setOnClickListener {
            val intent = Intent()
                .setType("application/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select a theme"), addTheme)
        }

        val adapter =
            GridThemeAdapter(requireActivity(), themeList, homeViewModel, addItemSelect = { _, _ ->
                toolbar.title = "${themeList.count { it.selected }}"
            }, removeItemSelect = { _, _ ->
                toolbar.title = "${themeList.count { it.selected }}"
            }, selectToggle = {
                toolbar.title = "${themeList.count { theme -> theme.selected }}"
                toggleToolbar(it)
            })

        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back, null)
        toolbar.setNavigationOnClickListener {
            themeList.forEachIndexed { index, _ -> themeList[index].selected = false }
            adapter.notifyDataSetChanged()
            toggleToolbar(false)
        }
        toolbar.setOnMenuItemClickListener { it ->
            when (it.itemId) {
                R.id.theme_delete -> {
                    MaterialDialog(requireContext()).show {
                        cornerRadius(12F)
                        message(res = R.string.delete_themes_confirm)
                        positiveButton(res = R.string.yes) { dialog ->
                            var noError = false
                            themeList.filter { theme -> theme.selected }
                                .forEachIndexed { index, theme ->
                                    if (theme.delete() && !noError) noError = true
                                }
                            if (noError) Toast.makeText(
                                context,
                                R.string.themes_deleted,
                                Toast.LENGTH_LONG
                            ).show()
                            else Toast.makeText(context, R.string.errors, Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                            toggleToolbar(false)
                            homeViewModel.setRefetch(true)
                        }
                        negativeButton(res = R.string.no) { dialog ->
                            dialog.dismiss()
                        }
                    }
                }
                R.id.select_all -> {
                    themeList.forEachIndexed { index, _ -> themeList[index].selected = true }
                    adapter.notifyDataSetChanged()
                    toolbar.title = "${themeList.count { it.selected }}"
                }
                R.id.theme_share -> {
                    ThemeHelper.shareThemes(requireActivity(), themeList.filter { it.selected })
                }
            }
            true
        }

        homeViewModel.gridLayoutObserve(this, Observer {
            recyclerView.stopScroll()
            adapter.dataSetChanged()
            if (recyclerView.layoutManager is GridLayoutManager) {
                val columns = if (it == GridLayout.SINGLE) 1 else 2
                (recyclerView.layoutManager as GridLayoutManager).spanCount = columns
                for (i in 0 until recyclerView.itemDecorationCount) {
                    recyclerView.removeItemDecorationAt(0)
                }
                recyclerView.addItemDecoration(
                    GridTopOffsetItemDecoration(
                        requireContext().getStatusBarHeight(),
                        columns
                    )
                )
                recyclerView.addItemDecoration(
                    GridBottomOffsetItemDecoration(
                        56.dpToPx(requireContext()).toInt(),
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
            adapter.dataSetChanged()
            recyclerView.scheduleLayoutAnimation()
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
            themeList.clear(adapter)
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
                    adapter.dataSetChanged()
                    refreshLayout.isRefreshing = false
                    if (homeViewModel.getRefetch()) homeViewModel.setRefetch(false)
                    recyclerView.scheduleLayoutAnimation()
                }
                keyboardImg.alpha = if (themeList.size > 0) 0F else 1F
            }.start()
        })

        delayed(200) {
            if (!homeViewModel.themesExist()) {
                Thread {
                    loadThemes().apply {
                        themeList.clear(adapter)
                        themeList.addAll(sortedBy { it.name.toLowerCase(Locale.ROOT) })
                    }
                    activity?.runOnUiThread {
                        homeViewModel.setThemes(themeList)
                        recyclerView.stopScroll()
                        adapter.dataSetChanged()
                        refreshLayout.isRefreshing = false
                        recyclerView.scheduleLayoutAnimation()
                    }
                }.start()
            } else {
                themeList.clear(adapter)
                themeList.addAll(homeViewModel.getThemes())
                recyclerView.stopScroll()
                adapter.dataSetChanged()
                refreshLayout.isRefreshing = false
                recyclerView.scheduleLayoutAnimation()
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
                requireContext().getStatusBarHeight(),
                columns
            )
        )
        recyclerView.addItemDecoration(
            GridBottomOffsetItemDecoration(
                56.dpToPx(requireContext()).toInt(),
                columns
            )
        )

        homeViewModel.getRecyclerViewState().apply {
            if (this != null) {
                recyclerView.layoutManager?.onRestoreInstanceState(this)
            }
        }

        ItemTouchHelper(object : SwipeToDeleteCallback(requireActivity()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = adapter.getItem(position)

                var restored = false

                adapter.removeItem(position)

                val snackBar = Snackbar.make(v, R.string.removed, Snackbar.LENGTH_LONG)
                snackBar.setAction(R.string.undo) {
                    restored = true
                    adapter.restoreItem(position, item)
                }

                snackBar.setActionTextColor(Color.YELLOW)
                snackBar.show()

                snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        if (!restored) item.delete()
                    }
                })
            }
        }).attachToRecyclerView(recyclerView)

        return v
    }

    private fun toggleToolbar(visible: Boolean) {
        val scale = if (visible) 1F else .0F
        val margin = 0
        val corners1 = if (visible) 20.dpToPx(activity!!) else 0F
        val corners2 = if (visible) 0F else 20.dpToPx(activity!!)
        val longDelay = if (visible) 100L else 800L

        val anim = ObjectAnimator.ofFloat(toolbar, "alpha", scale).apply {
            duration = 180
            if (visible) {
                toolbar.visibility = View.VISIBLE
            }
            start()
        }
        anim.doOnEnd {
            if (!visible) { toolbar.visibility = View.GONE }
        }

        toolbar.setMargins(0, margin, 0, 0)
    }

    private fun getShape(radius: Float): Drawable {
        val color = resources.getColor(R.color.colorPrimary, null)
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(color, color)
        )
        gradientDrawable.cornerRadius = radius
        return gradientDrawable
    }

    @SuppressLint("InflateParams", "SdCardPath")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == addTheme && resultCode == RESULT_OK && data != null && data.data != null) {
            val zip =
                File(
                    FileUtils.getThemePacksPath(context!!),
                    data.data!!.getFileName(activity!!)
                ).apply { data.data!!.writeToFile(context!!, this) }
            if (ThemeHelper.installTheme(zip, false)) Toast.makeText(
                context,
                R.string.theme_added,
                Toast.LENGTH_SHORT
            ).show()
            homeViewModel.setRefetch(true)
        }
    }

    override fun onDetach() {
        super.onDetach()
        homeViewModel.setRecyclerViewState(recyclerView.layoutManager?.onSaveInstanceState())
    }

    class GridThemeAdapter(
        private val context: FragmentActivity,
        private val list: ArrayList<ThemeDataClass>,
        private val homeViewModel: HomeViewModel,
        private val selectToggle: (selectOn: Boolean) -> Unit = {},
        private val addItemSelect: (theme: ThemeDataClass, index: Int) -> Unit = { _, _ -> },
        private val removeItemSelect: (theme: ThemeDataClass, index: Int) -> Unit = { _, _ -> }
    ) :
        RecyclerView.Adapter<GridThemeAdapter.ViewHolder>() {

        private var recyclerView: RecyclerView? = null
        private var lastPosition =
            recyclerView?.layoutManager?.let { (it as GridLayoutManager).findLastVisibleItemPosition() }
                ?: 0

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

        fun removeItem(position: Int) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }

        fun restoreItem(position: Int, item: ThemeDataClass) {
            list.add(position, item)
            notifyItemInserted(position)
        }

        fun getItem(position: Int): ThemeDataClass {
            return list[position]
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
                    holder.selectOverlay.animate().alpha(1F - holder.selectOverlay.alpha)
                        .setDuration(200).withEndAction {
                            notifyDataSetChanged()
                            if (list[position].selected) addItemSelect(dataClass, position)
                            if (!list[position].selected) removeItemSelect(dataClass, position)
                            if (!list.map { it.selected }.contains(true)) selectToggle(false)
                        }.start()
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
                holder.selectOverlay.animate().alpha(1F).setDuration(200).withEndAction {
                    notifyDataSetChanged()
                    selectToggle(true)
                }
                true
            }

            timingLogger.addSplit("Longclick Listener")

            timingLogger.dumpToLog()
            setAnimation(holder.card, position)
        }

        fun dataSetChanged() {
            lastPosition =
                recyclerView?.layoutManager?.let { (it as GridLayoutManager).findLastVisibleItemPosition() }
                    ?: 0
            notifyDataSetChanged()
        }

        private fun setAnimation(viewToAnimate: View, position: Int) {
            if (position > lastPosition) {
                val animation =
                    AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)
                viewToAnimate.startAnimation(animation)
                lastPosition = position
            }
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
}
