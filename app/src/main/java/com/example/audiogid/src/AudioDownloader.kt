package com.example.audiogid.src

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object AudioDownloader {

    fun download(
        context: Context,
        urlString: String,
        fileName: String
    ): String {

        // Создаём URL
        val url = URL(urlString)

        // Открываем соединение
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.connect()

            // Проверяем HTTP-код ответа
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception(
                    "Ошибка сервера: ${connection.responseCode}"
                )
            }

            // Файл будет сохранён во внутреннем хранилище приложения
            val file = File(context.filesDir, fileName)

            // Получаем поток с данными из интернета
            connection.inputStream.use { input ->

                // Создаём поток для записи файла
                file.outputStream().use { output ->

                    // Буфер на 8 КБ
                    val buffer = ByteArray(8 * 1024)

                    var bytesRead: Int

                    // Читаем данные частями
                    while (input.read(buffer).also { bytesRead = it } != -1) {

                        // Записываем очередную часть в файл
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            // Возвращаем полный путь к скачанному файлу
            return file.absolutePath

        } finally {
            // Закрываем интернет-соединение
            connection.disconnect()
        }
    }
}