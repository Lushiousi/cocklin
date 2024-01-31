package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject

@Suppress("NAME_SHADOWING")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiKey = "BQDDzjYz9Y7c1huv1DkRSkC05mH_ByJLkrdil6z9Vie2_VzONhG5A_x4G478zwerkFcLdh-grcpyPpA02hyuMtD36s-_cG_FVJiAg9l1dn7MsLrGV34"
        val trackName = "4x4 Big Baby Tape"

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/search?q=$trackName&type=track")
            .header("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                println("Failed to make the request: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle the response here
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    println(json)
                    val trackInfo = json.getJSONObject("tracks").getJSONArray("items").getJSONObject(0)
                    val trackName = trackInfo.getString("name")
                    val albumName = trackInfo.getJSONObject("album").getString("name")
                    val artistName = trackInfo.getJSONArray("artists").getJSONObject(0).getString("name")
                    println("Track: $trackName, Album: $albumName, Artist: $artistName")
                } else {
                    println("Failed to get track information")
                }
            }
        })
    }
}
