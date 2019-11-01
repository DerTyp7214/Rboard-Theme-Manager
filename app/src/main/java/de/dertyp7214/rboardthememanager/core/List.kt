package de.dertyp7214.rboardthememanager.core

import android.os.Looper
import de.dertyp7214.rboardthememanager.fragments.HomeGridFragment

fun <E> ArrayList<E>.clear(adapter: HomeGridFragment.GridThemeAdapter) {
    clear()
    if (Looper.getMainLooper().isCurrentThread)
        adapter.notifyDataSetChanged()
}