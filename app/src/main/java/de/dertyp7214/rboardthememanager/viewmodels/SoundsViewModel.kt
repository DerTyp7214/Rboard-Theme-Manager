@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.dertyp7214.rboardthememanager.viewmodels

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import androidx.core.content.edit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import de.dertyp7214.rboardthememanager.enums.GridLayout

class SoundsViewModel : ViewModel() {

    private val gridLayout = MutableLiveData<GridLayout>()
    private val recyclerViewState = MutableLiveData<Parcelable>()
    private val keyboardHeight = MutableLiveData<Int>()
    private val filter = MutableLiveData<String>()
    private val refetch = MutableLiveData<Boolean>()

    fun setRefetch(r: Boolean) {
        refetch.value = r
    }

    fun getFilter(): String {
        return filter.value ?: ""
    }

    fun setFilter(f: String) {
        filter.value = f
    }

    fun observeFilter(owner: LifecycleOwner, observer: Observer<String>) {
        filter.observe(owner, observer)
    }

    fun getKeyboardHeight(): Int {
        return keyboardHeight.value ?: 0
    }

    fun setKeyboardHeight(value: Int) {
        keyboardHeight.value = value
    }

    fun keyboardHeightObserver(owner: LifecycleOwner, observer: Observer<Int>) {
        keyboardHeight.observe(owner, observer)
    }

    fun getRecyclerViewState(): Parcelable? {
        return recyclerViewState.value
    }

    fun setRecyclerViewState(state: Parcelable?) {
        recyclerViewState.value = state
    }

    fun gridLayoutObserve(owner: LifecycleOwner, observer: Observer<GridLayout>) {
        gridLayout.observe(owner, observer)
    }

    fun getGridLayout(): GridLayout {
        return gridLayout.value ?: GridLayout.SINGLE
    }

    fun setGridLayout(value: GridLayout) {
        gridLayout.value = value
    }

    fun setGridLayout(value: GridLayout, activity: Activity) {
        setGridLayout(value)
        saveToStorage(activity)
    }

    fun loadFromStorage(activity: Activity) {
        activity.getSharedPreferences(this.javaClass.name, Context.MODE_PRIVATE).apply {
            when (getString("grid", GridLayout.SINGLE.name)) {
                GridLayout.SINGLE.name -> setGridLayout(GridLayout.SINGLE)
                GridLayout.SMALL.name -> setGridLayout(GridLayout.SMALL)
                GridLayout.BIG.name -> setGridLayout(GridLayout.BIG)
            }
        }
    }

    private fun saveToStorage(activity: Activity) {
        activity.getSharedPreferences(this.javaClass.name, Context.MODE_PRIVATE).edit {
            putString("grid", getGridLayout().name)
        }
    }
}