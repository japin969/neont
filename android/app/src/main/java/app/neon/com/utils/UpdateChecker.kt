package app.neon.com.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    // Ganti URL ini ke raw GitHub lu
    private const val VERSION_URL = "https://raw.githubusercontent.com/japin969/neont/main/version.json"

    data class UpdateInfo(
        val latestVersionCode: Int,
        val latestVersionName: String,
        val forceUpdate: Boolean,
        val message: String
    )

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(VERSION_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val response = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            
            UpdateInfo(
                latestVersionCode = json.getInt("latestVersionCode"),
                latestVersionName = json.getString("latestVersionName"),
                forceUpdate = json.getBoolean("forceUpdate"),
                message = json.getString("message")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null // Kalau offline/gagal, return null (aplikasi tetep jalan)
        }
    }
}
