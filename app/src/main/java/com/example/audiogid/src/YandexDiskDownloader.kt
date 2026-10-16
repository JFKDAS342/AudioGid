
package com.example.audiogid.src

import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object YandexDiskDownloader {

    fun getDownloadUrl(
        publicFolderUrl: String,
        fileName: String
    ): String {

        // Формируем запрос к API Яндекс Диска
        val apiUrl = Uri.parse(
            "https://cloud-api.yandex.net/v1/disk/public/resources/download"
        ).buildUpon()
            .appendQueryParameter("public_key", publicFolderUrl)
            .appendQueryParameter("path", "/$fileName")
            .build()
            .toString()

        val connection =
            URL(apiUrl).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            val code = connection.responseCode

            if (code != HttpURLConnection.HTTP_OK) {
                throw Exception("Яндекс Диск вернул ошибку: HTTP $code")
            }

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            // API возвращает JSON с полем href
            val json = JSONObject(response)

            return json.getString("href")

        } finally {
            connection.disconnect()
        }
    }
}
