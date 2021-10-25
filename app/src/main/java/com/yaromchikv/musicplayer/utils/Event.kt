package com.yaromchikv.musicplayer.utils

open class Event<out T>(private val data: T) {

    var hasBeenHandled = false
        private set

    fun getContentForHandled(): T? {
        return if (!hasBeenHandled) {
            hasBeenHandled = true
            data
        } else null
    }
}
