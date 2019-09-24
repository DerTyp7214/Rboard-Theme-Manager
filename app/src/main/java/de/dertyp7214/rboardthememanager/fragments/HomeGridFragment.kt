package de.dertyp7214.rboardthememanager.fragments

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.getBitmap
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.utils.ColorUtils.dominantColor
import de.dertyp7214.rboardthememanager.utils.ColorUtils.isColorLight
import de.dertyp7214.rboardthememanager.utils.ThemeUtils.loadThemes

class HomeGridFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val themeList = ArrayList<ThemeDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_home_grid, container, false)

        recyclerView = v.findViewById(R.id.theme_list)

        val adapter = GridThemeAdapter(context!!, themeList)

        Thread {
            themeList.clear()
            themeList.addAll(loadThemes())
            activity!!.runOnUiThread {
                adapter.notifyDataSetChanged()
                ObjectAnimator.ofFloat(recyclerView, "alpha", 1F).apply {
                    duration = 300
                    startDelay = 200
                    start()
                }
            }
        }.start()

        val layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        return v
    }

    class GridThemeAdapter(private val context: Context, private val list: List<ThemeDataClass>) :
        RecyclerView.Adapter<GridThemeAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.theme_grid_item,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val selection = list.map { it.selected }.contains(true)
            val dataClass = list[position]

            val default = context.resources.getDrawable(
                R.drawable.ic_keyboard,
                null
            ).getBitmap()
            val color = dominantColor(dataClass.image ?: default)

            holder.themeImage.setImageBitmap(dataClass.image ?: default)
            holder.themeImage.alpha = if (dataClass.image != null) 1F else .3F

            holder.themeName.text = dataClass.name
            holder.themeNameSelect.text = dataClass.name

            holder.themeName.setTextColor(if (isColorLight(color)) Color.BLACK else Color.WHITE)

            if (dataClass.selected)
                holder.selectOverlay.alpha = 1F
            else
                holder.selectOverlay.alpha = 0F

            holder.card.setCardBackgroundColor(color)

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
                        notifyDataSetChanged()
                    }
                }
            }

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
                    notifyDataSetChanged()
                }
                true
            }
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val themeImage: ImageView = v.findViewById(R.id.theme_image)
            val themeName: TextView = v.findViewById(R.id.theme_name)
            val themeNameSelect: TextView = v.findViewById(R.id.theme_name_selected)
            val selectOverlay: LinearLayout = v.findViewById(R.id.select_overlay)
            val card: CardView = v.findViewById(R.id.card)
        }
    }
}
