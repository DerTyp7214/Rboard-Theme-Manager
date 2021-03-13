package de.dertyp7214.rboardthememanager.component

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.data.MenuItem

class MenuBottomSheet(
    private val items: List<MenuItem>,
    private val title: String = "",
    private val customHeader: View? = null
) :
    RoundedBottomSheetDialogFragment() {

    constructor() : this(ArrayList()) {
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.menu_bottom_sheet, container, false)

        v.findViewById<TextView>(R.id.title).apply {
            text = title
            if (title.isEmpty()) visibility = GONE
        }

        v.findViewById<LinearLayout>(R.id.ll).apply {
            if (customHeader != null) addView(customHeader, 0)
        }

        val recyclerView: RecyclerView = v.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                return ViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.menu_item,
                        parent,
                        false
                    )
                )
            }

            override fun getItemCount(): Int = items.size

            @SuppressLint("NotifyDataSetChanged")
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val item = items[position]

                val accent = resources.getColor(R.color.colorAccent, null)
                val primary = resources.getColor(R.color.primaryText, null)

                holder.text.setText(item.string)
                holder.text.setTextColor(if (item.selected) accent else primary)

                if (item.icon != null) {
                    holder.icon.setImageResource(item.icon)
                    holder.icon.imageTintList =
                        ColorStateList.valueOf(if (item.selected) accent else primary)
                }

                ObjectAnimator.ofInt(
                    holder.card,
                    "cardBackgroundColor",
                    if (item.selected) ColorUtils.setAlphaComponent(
                        accent,
                        77
                    ) else Color.TRANSPARENT
                ).apply {
                    setEvaluator(ArgbEvaluator())
                    duration = 200
                    start()
                }

                holder.click.setOnClickListener {
                    if (!item.selected) {
                        item.func(position)
                        items.forEachIndexed { index, _ ->
                            items[index].selected = index == position
                        }
                        notifyDataSetChanged()
                        if (item.closeOnClick) dismiss()
                    }
                }
            }
        }

        return v
    }
}

class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val click: LinearLayout = v.findViewById(R.id.click)
    val card: CardView = v.findViewById(R.id.card)
    val icon: ImageView = v.findViewById(R.id.icon)
    val text: TextView = v.findViewById(R.id.text)
}