package com.example.audiogid.src

import android.media.MediaPlayer
import java.io.File
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
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private val publicFolderUrl =
        "https://disk.yandex.ru/d/QNVM3-QBRe9uZg"
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExhibitAdapter
    private var gradientView: View? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var playerTitle: TextView
    private lateinit var audioSeekBar: SeekBar

    private val playerHandler = Handler(Looper.getMainLooper())
    private var isPrepared = false
    private var isUserSeeking = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerTitle = findViewById(R.id.playerTitle)
        audioSeekBar = findViewById(R.id.audioSeekBar)

        findViewById<Button>(R.id.buttonPause).setOnClickListener {
            if (isPrepared && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        }

        findViewById<Button>(R.id.buttonResume).setOnClickListener {
            if (isPrepared && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                playerHandler.post(updateProgress)
            }
        }

        findViewById<Button>(R.id.buttonStop).setOnClickListener {
            stopAudio()
            playerTitle.text = "Аудиогид остановлен"
            audioSeekBar.progress = 0
        }

        audioSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    isUserSeeking = true
                }

                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser && isPrepared) {
                        val duration = mediaPlayer?.duration ?: 0
                        val newPosition = duration * progress / 1000
                        mediaPlayer?.seekTo(newPosition)
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    isUserSeeking = false
                }
            }
        )

        gradientView = findViewById<View>(R.id.gradientTwo)?.also {
            AnimationHelper.startGradientAnimation(it)
        }

        recyclerView = findViewById(R.id.exhibitRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ExhibitAdapter(emptyList<Exhibit>()) { exhibit ->
            onExhibitClick(exhibit)
        }
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

    private fun onExhibitClick(exhibit: Exhibit) {
        val path = exhibit.localPath

        if (path == null) {
            // Аудио ещё не скачано
            onDownload(exhibit)
        } else {
            // Аудио уже скачано
            playAudio(path, exhibit.title)
        }
    }

    private fun stopAudio() {
        playerHandler.removeCallbacks(updateProgress)

        val player = mediaPlayer
        mediaPlayer = null
        isPrepared = false

        if (player != null) {
            try {
                player.stop()
            } catch (_: IllegalStateException) {
                // Плеер мог ещё не подготовиться
            } finally {
                player.release()
            }
        }

        audioSeekBar.progress = 0
    }

    private fun playAudio(path: String, title: String) {
        val audioFile = File(path)

        // Проверяем, существует ли файл
        if (!audioFile.exists()) {
            Toast.makeText(
                this,
                "Аудиофайл не найден. Скачай его заново.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        // Останавливаем предыдущую запись, если она играла
        stopAudio()

        try {
            mediaPlayer = MediaPlayer().apply {

                // Указываем путь к MP3 на телефоне
                setDataSource(audioFile.absolutePath)

                // Когда файл будет готов к воспроизведению
                setOnPreparedListener { player ->
                    isPrepared = true
                    audioSeekBar.progress = 0
                    audioSeekBar.max = 1000
                    playerTitle.text = "Сейчас играет: $title"

                    player.start()
                    playerHandler.removeCallbacks(updateProgress)
                    playerHandler.post(updateProgress)

                    Toast.makeText(
                        this@MainActivity,
                        "Воспроизводится: $title",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Когда аудио закончится
                setOnCompletionListener {
                    stopAudio()
                }

                // Если возникла ошибка воспроизведения
                setOnErrorListener { _, _, _ ->
                    stopAudio()

                    Toast.makeText(
                        this@MainActivity,
                        "Не удалось воспроизвести аудио",
                        Toast.LENGTH_LONG
                    ).show()

                    true
                }

                // Подготавливаем аудио без блокировки интерфейса
                prepareAsync()
            }

        } catch (e: Exception) {
            stopAudio()

            Toast.makeText(
                this,
                "Ошибка воспроизведения: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val updateProgress = object : Runnable {
        override fun run() {
            val player = mediaPlayer

            if (isPrepared && player != null) {
                val duration = player.duration

                if (duration > 0 && !isUserSeeking) {
                    audioSeekBar.progress =
                        player.currentPosition * 1000 / duration
                }

                if (player.isPlaying) {
                    playerHandler.postDelayed(this, 500)
                }
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

                    // Получаем прямую ссылку на нужный MP3
                    val downloadUrl = YandexDiskDownloader.getDownloadUrl(
                        publicFolderUrl = publicFolderUrl,
                        fileName = exhibit.audioName
                    )

                    // Скачиваем MP3 на телефон
                    AudioDownloader.download(
                        context = applicationContext,
                        urlString = downloadUrl,
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
