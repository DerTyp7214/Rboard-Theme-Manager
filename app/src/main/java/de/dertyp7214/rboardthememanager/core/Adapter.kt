package de.dertyp7214.rboardthememanager.core

import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.Adapter<*>.notifyItemsRemoved(items: IntArray) {
    items.forEach {
        notifyItemRemoved(it + 1)
    }
}