package de.dertyp7214.rboardthememanager.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import de.dertyp7214.rboardthememanager.enum.GridLayout

class HomeViewModel : ViewModel() {

    private val gridLayout = MutableLiveData<GridLayout>()

    fun gridLayoutObserve(owner: LifecycleOwner, observer: Observer<GridLayout>) {
        gridLayout.observe(owner, observer)
    }

    fun getGridLayout(): GridLayout {
        return gridLayout.value ?: GridLayout.SMALL
    }

    fun setGridLayout(value: GridLayout) {
        gridLayout.value = value
    }
}