package com.yaromchikv.musicplayer.data

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val context: Context
) {

    fun getAllSongs(): List<Song> {
        val json = loadJSONFromAssets()
        if (json != null) {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val type = Types.newParameterizedType(List::class.java, Song::class.java)
            val adapter: JsonAdapter<List<Song>> = moshi.adapter(type)
            return adapter.fromJson(json) ?: emptyList()
        }
        return emptyList()
    }

    private fun loadJSONFromAssets(): String? {
        return try {
            val stream: InputStream = context.assets.open("music.json")
            val buffer = ByteArray(stream.available())
            stream.run {
                read(buffer)
                close()
            }
            String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}