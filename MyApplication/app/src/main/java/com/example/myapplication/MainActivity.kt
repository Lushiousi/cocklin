package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {

    val client = OkHttpClient()
    val apiKey = "y0_AgAAAABo1afiAAG8XgAAAAD5dA4KAABYvRdegUNCP43m6gd3QW5jZ8gCpw"
    val tracksList = ArrayList<String>()
    val PAGE_SIZE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.searchButton)
        val searchEditText = findViewById<EditText>(R.id.searchEditText)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            searchTracks(query, 0) // Начинаем с нулевой страницы
        }
    }

    private fun searchTracks(query: String, page: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val url = "https://api.music.yandex.net/search?type=track&text=$query&page=$page&page_size=$PAGE_SIZE"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            try {
                val response: Response = client.newCall(request).execute()
                val responseData = response.body?.string()

                processSearchResults(responseData)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun processSearchResults(responseData: String?) {
        println(responseData)
        responseData?.let {
            val result = Gson().fromJson(responseData, SearchResult::class.java)
            result.result?.tracks?.let { tracksList.addAll(it.map { track -> "${track.title} by ${getArtistsString(track.artists)}" }) }

            // Обновление пользовательского интерфейса в главной корутине
            GlobalScope.launch(Dispatchers.Main) {
                displaySearchResults()
            }
        }
    }

    private fun getArtistsString(artists: List<Artist>?): String {
        if (artists.isNullOrEmpty()) {
            return "Unknown"  // Если информация об исполнителях отсутствует
        }
        val artistNames = artists.map { it.name }
        return artistNames.joinToString(", ")
    }
}

    private fun displaySearchResults() {
        val resultTextView = findView<TextView>(R.id.resultTextView)
        val resultText = StringBuilder()
        for (track in tracksList) {
            resultText.append("$track\n")
        }
        resultTextView.text = resultText.toString()
    }

data class SearchResult(
    val result: Result?
)

data class Result(
    val tracks: List<Track>?
)

data class Track(
    val title: String,
    val artists: List<Artist>  // Добавляем список исполнителей трека
)

data class Artist(
    val name: String
)

