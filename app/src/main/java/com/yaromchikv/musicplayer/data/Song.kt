package com.yaromchikv.musicplayer.data

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val bitmapUri: String,
    val trackUri: String
)