package de.dertyp7214.rboardthememanager.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.dertyp7214.rboardthememanager.fragments.SelectRuntimeData

class IntroViewModel : ViewModel() {

    val selectRuntimeData = MutableLiveData<SelectRuntimeData>()
    val rboardStorage = MutableLiveData<Boolean>()
    val gboardStorage = MutableLiveData<Boolean>()
    val selected = MutableLiveData<Boolean>()
    val open = MutableLiveData<Boolean>()

    fun setSystem(value: Boolean) {
        if (selectRuntimeData.value == null) selectRuntimeData.value =
            SelectRuntimeData(system = value)
        else selectRuntimeData.value?.system = value
    }

    fun setMagisk(value: Boolean) {
        if (selectRuntimeData.value == null) selectRuntimeData.value =
            SelectRuntimeData(magisk = value)
        else selectRuntimeData.value?.magisk = value
    }

    fun setRboardPermission(value: Boolean) {
        rboardStorage.value = value
    }

    fun setGboardPermission(value: Boolean) {
        gboardStorage.value = value
    }

    fun rboardPermission(): Boolean {
        return rboardStorage.value ?: false
    }

    fun gboardPermission(): Boolean {
        return gboardStorage.value ?: false
    }
}