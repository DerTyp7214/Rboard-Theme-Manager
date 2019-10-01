package de.dertyp7214.rboardthememanager.data

data class MenuItem(
    val icon: Int,
    val string: Int,
    var selected: Boolean,
    val func: (index: Int) -> Unit
)