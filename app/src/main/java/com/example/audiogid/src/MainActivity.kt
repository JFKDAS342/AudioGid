package com.example.audiogid.src

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiogid.R
import com.example.audiogid.data.AppDatabase
import com.example.audiogid.data.Exhibit
import com.example.audiogid.data.ExhibitAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExhibitAdapter
    private var gradientView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gradientView = findViewById<View>(R.id.gradientTwo)?.also {
            AnimationHelper.startGradientAnimation(it)
        }

        recyclerView = findViewById(R.id.exhibitRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ExhibitAdapter(emptyList<Exhibit>()) { exhibit -> onDownload(exhibit) }
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            try {
                val inserted = withContext(Dispatchers.IO) { fillDatabase() }
                if (inserted > 0) {
                    Toast.makeText(
                        this@MainActivity,
                        "Добавлено записей: $inserted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                loadExhibits()
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Ошибка загрузки: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {

        gradientView?.let { AnimationHelper.stopAnimation(it) }
        gradientView = null
        super.onDestroy()
    }

    private suspend fun fillDatabase(): Int {
        val dao = AppDatabase.getDatabase(applicationContext).exhibitDao()

        if (dao.count() > 0) return 0

        val exhibits = listOf(
            Exhibit(title = "Звёздная ночь", author = "Винсент ван Гог", audioName = "starry_night.mp3"),
            Exhibit(title = "Крик", author = "Эдвард Мунк", audioName = "the_scream.mp3"),
            Exhibit(title = "Мона Лиза", author = "Леонардо да Винчи", audioName = "mona_lisa.mp3"),
            Exhibit(title = "Девушка с жемчужной серёжкой", author = "Ян Вермеер", audioName = "girl_pearl.mp3"),
            Exhibit(title = "Герника", author = "Пабло Пикассо", audioName = "guernica.mp3")
        )

        dao.insertAll(exhibits)
        return exhibits.size
    }

    private suspend fun loadExhibits() {
        // Room suspend-функции main-safe, можно вызывать на Main
        val exhibits = AppDatabase.getDatabase(applicationContext).exhibitDao().getAll()
        adapter.submitList(exhibits)
    }

    private fun getAudioUrl(exhibit: Exhibit): String {

        return when (exhibit.title) {

            "Звёздная ночь" ->
                "https://example.com/starry_night.mp3"

            "Крик" ->
                "https://example.com/the_scream.mp3"

            "Мона Лиза" ->
                "https://example.com/mona_lisa.mp3"

            "Девушка с жемчужной серёжкой" ->
                "https://example.com/girl_pearl.mp3"

            "Герника" ->
                "https://example.com/guernica.mp3"

            else ->
                throw Exception("Неизвестная картина")
        }
    }

    private fun onDownload(exhibit: Exhibit) {

        lifecycleScope.launch {

            try {

                Toast.makeText(
                    this@MainActivity,
                    "Скачивание: ${exhibit.title}",
                    Toast.LENGTH_SHORT
                ).show()

                val filePath = withContext(Dispatchers.IO) {

                    AudioDownloader.download(
                        context = applicationContext,
                        urlString = getAudioUrl(exhibit),
                        fileName = exhibit.audioName
                    )
                }

                // Получаем DAO
                val dao = AppDatabase
                    .getDatabase(applicationContext)
                    .exhibitDao()

                // Сохраняем путь к MP3 в Room
                dao.updateLocalPath(
                    exhibit.id,
                    filePath
                )

                // Обновляем список на экране
                loadExhibits()

                Toast.makeText(
                    this@MainActivity,
                    "Аудио скачано!",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {

                Toast.makeText(
                    this@MainActivity,
                    "Ошибка скачивания: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
