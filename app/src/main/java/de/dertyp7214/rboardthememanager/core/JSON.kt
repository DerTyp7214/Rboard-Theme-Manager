package de.dertyp7214.rboardthememanager.core

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.safeParse(string: String): JSONObject {
    return try {
        JSONObject(string)
    } catch (e: Exception) {
        JSONObject()
    }
}

fun JSONArray.safeParse(string: String): JSONArray {
    return try {
        JSONArray(string)
    } catch (e: Exception) {
        JSONArray()
    }
}

fun JSONArray.forEach(run: (o: Any, index: Int) -> Unit) {
    for (i in 0 until length()) run(get(i), i)
}