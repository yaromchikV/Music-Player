package com.yaromchikv.musicplayer.model

data class Track(
    val id: Int,
    val title: String,
    val artist: String,
    val bitmapUri: String,
    val trackUri: String,
    val duration: Long
)