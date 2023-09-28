package com.maykish.crmcast

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.maykish.crmcast.model.RssFeed
import com.maykish.crmcast.model.RssItem
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.simpleframework.xml.core.Persister
import java.io.IOException


class MainActivity : ComponentActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    private var playing = false

    private var episodes: List<RssItem>? = null

    private var episodeIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayer = MediaPlayer()

        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA).build()
        )

        val playButton = findViewById<Button>(R.id.myButton)
        val nowPlaying = findViewById<TextView>(R.id.nowPlaying)

        playButton.setOnClickListener {
            Toast.makeText(this, "Playing MP3", Toast.LENGTH_LONG).show()

            if (playing) {
                mediaPlayer.stop()
                playing = false
                playButton.text = "PLAY"
                nowPlaying.text = ""
            } else {
                mediaPlayer.setDataSource(episodes?.last()?.link.toString())    // TODO toString doesn't seem right
                Log.d("crm", "preparing media")
                mediaPlayer.prepare()   // TODO use prepareAsync to get this off main thread

                Log.d("crm", "playing media")
                mediaPlayer.start()

                playing = true
                playButton.text = "PAUSE"

                nowPlaying.text = episodes!![episodeIndex].title.toString()
                findViewById<TextView>(R.id.currentIndex).text = episodeIndex.toString()
            }
        }

        makeHttpRequest("https://feeds.megaphone.fm/FGTL8482782862")
    }

    private fun makeHttpRequest(targetURL: String) {
        // Create an instance of OkHttpClient
        val client = OkHttpClient()

        // Define the URL you want to make a request to
        // Create an HTTP request using Request.Builder
        val request = Request.Builder()
            .url(targetURL)
            .build()

        // Send the request using the OkHttpClient
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle network failures here
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle the response here
                if (response.isSuccessful) {
                    val xmlData = response.body?.string()
                    val serializer = Persister()
                    val rssFeed = serializer.read(RssFeed::class.java, xmlData)

                    episodes = rssFeed.channel?.items?.reversed()

                    if (episodes != null) {
                        val i = episodes!!.size - 1
                        Log.d("crm", "new index is: $i")
                        changeIndex(i)
                    }

                    if (episodes != null) {
                        for (i in episodes!!) {
                            Log.d("crm", i.title.toString() + ": " + i.link.toString())
                        }
                    } else {
                        Log.d("crm", "response is null")
                    }
                } else {
                    // Handle unsuccessful responses here
                    println("Request was not successful: ${response.code}")
                }
            }
        })
    }

    private fun changeIndex(newIndex: Int) {
        episodeIndex = newIndex
    }

}
