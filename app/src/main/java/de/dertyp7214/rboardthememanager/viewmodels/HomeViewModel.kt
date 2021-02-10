@file:Suppress("MemberVisibilityCanBePrivate")

package de.dertyp7214.rboardthememanager.viewmodels

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.os.Parcelable
import androidx.core.content.edit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.enums.GridLayout

class HomeViewModel : ViewModel() {

    private val gridLayout = MutableLiveData<GridLayout>()
    private val themes = MutableLiveData<ArrayList<ThemeDataClass>>()
    private val recyclerViewState = MutableLiveData<Parcelable>()
    private val keyboardHeight = MutableLiveData<Int>()
    private val filter = MutableLiveData<String>()
    private val filterDownloads = MutableLiveData<String>()
    private val refetch = MutableLiveData<Boolean>()
    private val refetchDownloads = MutableLiveData<Boolean>()

    fun getRefetchDownloads(): Boolean {
        return refetchDownloads.value == true
    }

    fun setRefetchDownloads(r: Boolean) {
        refetchDownloads.value = r
    }

    fun observeRefetchDownloads(owner: LifecycleOwner, observer: Observer<Boolean>) {
        refetchDownloads.observe(owner, observer)
    }

    fun getRefetch(): Boolean {
        return refetch.value == true
    }

    fun setRefetch(r: Boolean) {
        refetch.value = r
    }

    fun observeRefetch(owner: LifecycleOwner, observer: Observer<Boolean>) {
        refetch.observe(owner, observer)
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

    fun getFilterDownloads(): String {
        return filterDownloads.value ?: ""
    }

    fun setFilterDownloads(f: String) {
        filterDownloads.value = f
    }

    fun observeFilterDownloads(owner: LifecycleOwner, observer: Observer<String>) {
        filterDownloads.observe(owner, observer)
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

    fun themesExist(): Boolean {
        return themes.value != null
    }

    fun getThemes(): ArrayList<ThemeDataClass> {
        return themes.value ?: ArrayList()
    }

    fun setThemes(list: ArrayList<ThemeDataClass>) {
        themes.value = list
    }

    fun themesObserve(owner: LifecycleOwner, observer: Observer<ArrayList<ThemeDataClass>>) {
        themes.observe(owner, observer)
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
        activity.getSharedPreferences(this.javaClass.name, MODE_PRIVATE).apply {
            when (getString("grid", GridLayout.SINGLE.name)) {
                GridLayout.SINGLE.name -> setGridLayout(GridLayout.SINGLE)
                GridLayout.SMALL.name -> setGridLayout(GridLayout.SMALL)
                GridLayout.BIG.name -> setGridLayout(GridLayout.BIG)
            }
        }
    }

    fun saveToStorage(activity: Activity) {
        activity.getSharedPreferences(this.javaClass.name, MODE_PRIVATE).edit {
            putString("grid", getGridLayout().name)
        }
    }
}