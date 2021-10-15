package com.yaromchikv.musicplayer

fun Long.toTimer(): String {
    val hours = this / (1000 * 60 * 60)
    val minutes = (this / (1000 * 60)) % 60
    val seconds = (this / 1000) % 60

    return if (hours > 0) "$hours:" else "" +
            (if (minutes < 10) "0$minutes" else "$minutes") + ":" +
            if (seconds < 10) "0$seconds" else "$seconds"
}