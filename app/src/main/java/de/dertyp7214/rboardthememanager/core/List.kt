package de.dertyp7214.rboardthememanager.core

import android.os.Looper
import de.dertyp7214.rboardthememanager.fragments.HomeGridFragment

fun <E> ArrayList<E>.clear(adapter: HomeGridFragment.GridThemeAdapter) {
    clear()
    if (Looper.getMainLooper().isCurrentThread)
        adapter.dataSetChanged()
}

fun <E> List<E>.indexesOf(action: (item: E) -> Boolean): List<Int> {
    val tmp = ArrayList<Int>()
    forEachIndexed { index, item -> if (action(item)) tmp.add(index) }
    return tmp
}