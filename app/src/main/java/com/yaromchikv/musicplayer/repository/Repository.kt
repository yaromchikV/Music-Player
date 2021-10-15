package com.yaromchikv.musicplayer.repository

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yaromchikv.musicplayer.model.Track
import java.io.IOException
import java.io.InputStream

class Repository(private val context: Context) {

    val trackList: List<Track> by lazy {
        getTrackListFromJson()
    }

    fun mediaPlayerInstance(trackUri: String): MediaPlayer {
        return MediaPlayer.create(context, Uri.parse(trackUri))
    }

    private fun getTrackListFromJson(): List<Track> {
        val json = loadJSONFromAssets()
        if (json != null) {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val type = Types.newParameterizedType(List::class.java, Track::class.java)
            val adapter: JsonAdapter<List<Track>> = moshi.adapter(type)
            return adapter.fromJson(json) ?: emptyList()
        }
        return emptyList()
    }

    private fun loadJSONFromAssets(): String? {
        return try {
            val stream: InputStream = context.assets.open("music.json")
            val buffer = ByteArray(stream.available())
            stream.read(buffer)
            stream.close()
            String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}