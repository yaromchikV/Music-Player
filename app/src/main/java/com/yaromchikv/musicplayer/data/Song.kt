package com.yaromchikv.musicplayer.data

data class Song(
    val id: Int = 0,
    val title: String = "",
    val artist: String = "",
    val bitmapUri: String = "",
    val trackUri: String = "",
    val duration: Long = 0L
)