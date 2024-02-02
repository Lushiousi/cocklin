package com.example.myapplication

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var trackPreviewUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchTrackEditText = findViewById<EditText>(R.id.searchtrack)
        val trackInfoTextView = findViewById<TextView>(R.id.trackinfo)
        val searchBtn = findViewById<Button>(R.id.searchButton)
        val playBtn = findViewById<Button>(R.id.playButton)
        val pauseBtn = findViewById<Button>(R.id.pauseButton)

        val apiKey =
            "\"BQCQNxLfYKLs6eP0DSvffCIlsjFob8j8GU5mJ_1zbiNPlgMbQ61vzr2lvBfncm4ACXMjb-yajE6tcftTf76P42gjARzZwch7V13fGNWLr5F3n9dTv24"

        searchBtn.setOnClickListener {
            val trackName = searchTrackEditText.text.toString()
            fetchTrackInfo(trackName, apiKey, trackInfoTextView)
        }

        playBtn.setOnClickListener {
            playTrack()
        }

        pauseBtn.setOnClickListener {
            pauseTrack()
        }
    }

    private fun playTrack() {
        if (trackPreviewUrl != null) {
            if (mediaPlayer != null) {
                mediaPlayer!!.release()
                mediaPlayer = null
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setAudioAttributes(audioAttributes)

            try {
                mediaPlayer!!.setDataSource(trackPreviewUrl) // Установка URL для воспроизведения
                mediaPlayer!!.prepare()
                mediaPlayer!!.start()
            } catch (e: Exception) {
                // Обработка ошибок при установке URL для воспроизведения
                e.printStackTrace()
            }
        }
    }

    private fun pauseTrack() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
        }
    }

    private fun fetchTrackInfo(trackName: String, apiKey: String, trackInfoTextView: TextView) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/search?q=$trackName&type=track")
            .header("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                runOnUiThread {
                    trackInfoTextView.text = "Failed to make the request: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Обработка ответа от API
                if (response.isSuccessful && response.body != null) {
                    val json = JSONObject(response.body!!.string())
                    val items = json.getJSONObject("tracks").getJSONArray("items")

                    var trackInfo: JSONObject? = null
                    var trackPreviewUrl: String? = null

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val previewUrl = item.getString("preview_url")
                        if (previewUrl != "null") {
                            trackInfo = item
                            trackPreviewUrl = previewUrl
                            break
                        }
                    }

                    if (trackPreviewUrl != null) {
                        // Информация о найденном треке
                        val trackName = trackInfo!!.getString("name")
                        val albumName = trackInfo.getJSONObject("album").getString("name")
                        val artistName =
                            trackInfo.getJSONArray("artists").getJSONObject(0).getString("name")
                        val trackInfoText = "$trackName, $albumName, $artistName"
                        runOnUiThread {
                            trackInfoTextView.text = trackInfoText
                            println(trackInfo)
                        }
                        this@MainActivity.trackPreviewUrl = trackPreviewUrl
                    } else {
                        // Нет доступных треков
                        runOnUiThread {
                            trackInfoTextView.text = "No tracks available for preview"
                        }
                    }
                } else {
                    // Обработка неуспешного ответа от API
                }
            }
        })
    }
}
