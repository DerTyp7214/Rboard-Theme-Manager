package de.dertyp7214.rboardthememanager.auth

import de.dertyp7214.rboardthememanager.core.safeParse
import org.json.JSONObject
import java.net.URL

object Api {
    private const val apiUrl = "https://api.dertyp7214.de"

    fun register(key: String, email: String): Boolean {
        val response = JSONObject().safeParse(URL("$apiUrl/register/$key/$email").readText())
        return response.has("registered") && response.getBoolean("registered")
    }

    fun login(email: String): Boolean {
        val response = JSONObject().safeParse(URL("$apiUrl/login/$email").readText())
        return response.has("login") && response.getBoolean("login")
    }
}