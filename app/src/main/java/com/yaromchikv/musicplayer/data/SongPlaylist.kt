package com.yaromchikv.musicplayer.data

import android.content.res.AssetManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import javax.inject.Inject

class SongPlaylist @Inject constructor(
    private val assetManager: AssetManager
) {

    private var _catalog: List<Song>? = null
    val catalog: List<Song> get() = requireNotNull(_catalog)

    init {
        initPlaylistFromJson()
    }

    private fun initPlaylistFromJson() {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val arrayType = Types.newParameterizedType(List::class.java, Song::class.java)
        val adapter: JsonAdapter<List<Song>> = moshi.adapter(arrayType)
        val myJson = assetManager.open(FILE_NAME).bufferedReader().use { it.readText() }

        _catalog = adapter.fromJson(myJson)
    }

    companion object {
        const val FILE_NAME = "music.json"
    }
}
