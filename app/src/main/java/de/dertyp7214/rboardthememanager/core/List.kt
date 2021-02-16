package de.dertyp7214.rboardthememanager.core

import android.os.Looper
import de.dertyp7214.rboardthememanager.fragments.DownloadFragment
import de.dertyp7214.rboardthememanager.fragments.HomeGridFragment

fun <E> ArrayList<E>.clear(adapter: HomeGridFragment.GridThemeAdapter) {
    clear()
    if (Looper.getMainLooper().isCurrentThread)
        adapter.dataSetChanged()
}

fun <E> ArrayList<E>.clear(adapter: DownloadFragment.Adapter) {
    clear()
    if (Looper.getMainLooper().isCurrentThread)
        adapter.notifyDataSetChanged()
}

fun <E> List<E>.indexesOf(action: (item: E) -> Boolean): List<Int> {
    val tmp = ArrayList<Int>()
    forEachIndexed { index, item -> if (action(item)) tmp.add(index) }
    return tmp
}

fun <E> List<E>.containsAny(list: List<E>): Boolean {
    var contains = false
    forEach {
        contains = contains || list.contains(it)
        if (contains) return true
    }
    return contains
}

operator fun <E> List<E>.times(times: Int): ArrayList<E> {
    val list = ArrayList(this)
    for (i in 0..times) list.addAll(this)
    return list
}