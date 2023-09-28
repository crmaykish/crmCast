package com.maykish.crmcast

import android.media.MediaPlayer
import android.media.browse.MediaBrowser.MediaItem
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.android.exoplayer2.ExoPlayer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.core.Persister
import java.io.IOException


@Root(name = "item", strict = false)
data class RssItem(
    @field:Element(name = "title")
    var title: String? = null,

    @field:Element(name = "link")
    var link: String? = null,

//    @field:Element(name = "description")
//    var description: String? = null,

    // Add other fields you need from the RSS item
)

@Root(name = "rss", strict = false)
data class RssFeed(
    @field:Element(name = "channel")
    var channel: RssChannel? = null
)

@Root(name = "channel", strict = false)
data class RssChannel(
    @field:ElementList(inline = true, entry = "item")
    var items: List<RssItem>? = null
)

class MainActivity : ComponentActivity() {

    private var mp3Url = "https://sphinx.acast.com/p/open/s/6500f93e9654d100128055d8/e/a1312b36-4299-11ee-a351-9ffce0576419/media.mp3"

    private var playing = false;

    private var episodes: List<RssItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val player = ExoPlayer.Builder(this).build()

        val playButton = findViewById<Button>(R.id.myButton)

        playButton.setOnClickListener {
            Toast.makeText(this, "Playing MP3", Toast.LENGTH_LONG).show()

            if (playing)
            {
                player.stop()
                playing = false;
                playButton.text = "PLAY"
            }
            else {
                player.setMediaItem(com.google.android.exoplayer2.MediaItem.fromUri(episodes?.last()?.link.toString()))
                Log.d("crm", "preparing media")
                player.prepare()
                Log.d("crm", "playing media")
                player.play()
                playing = true;
                playButton.text = "PAUSE"
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

                    // Access the parsed RSS items
                    episodes = rssFeed.channel?.items

                    if (episodes != null) {
                        for (i in episodes!!) {
                            Log.d("crm", i.title.toString() + ": " + i.link.toString())
                        }
                    }
                    else {
                        Log.d("crm", "response is null")
                    }
                } else {
                    // Handle unsuccessful responses here
                    println("Request was not successful: ${response.code}")
                }
            }
        })
    }

}
