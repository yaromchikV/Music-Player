package com.yaromchikv.musicplayer.viewmodel

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yaromchikv.musicplayer.model.Track
import com.yaromchikv.musicplayer.repository.Repository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivityViewModel(private val repository: Repository) : ViewModel() {

    private val trackList: List<Track> = repository.trackList
    private var mediaPlayer: MediaPlayer

    private var job: Job? = null

    //region stateFlows
    private val _currentTrack = MutableLiveData(trackList.first())
    val currentTrack: LiveData<Track>
        get() = _currentTrack

    private val _currentPosition = MutableLiveData(0)
    val currentPosition: LiveData<Int>
        get() = _currentPosition

    private val _loadProgress = MutableLiveData(0L)
    val loadProgress: LiveData<Long>
        get() = _loadProgress

    private val _musicIsPlaying = MutableLiveData(false)
    val musicIsPlaying: LiveData<Boolean>
        get() = _musicIsPlaying
    //endregion

    init {
        _currentTrack.value = trackList.first()
        mediaPlayer = repository.mediaPlayerInstance(_currentTrack.value!!.trackUri)
        mediaPlayer.setOnBufferingUpdateListener { _, progress ->
            _loadProgress.postValue(mediaPlayer.duration * progress / 100L)
        }
    }

    //region clickListeners
    fun thePlayPauseButtonWasClicked() {
        if (!mediaPlayer.isPlaying) {
            job = jobLauncher()
            _musicIsPlaying.postValue(true)
            mediaPlayer.start()
        } else {
            job?.cancel()
            _musicIsPlaying.postValue(false)
            mediaPlayer.pause()
        }
    }

    private fun jobLauncher() = viewModelScope.launch {
        while (true) {
            _currentPosition.value = mediaPlayer.currentPosition
            delay(1000)
        }
    }

    fun playPrevTrack() {
        val prevId =
            if (_currentTrack.value?.id != trackList.first().id)
                _currentTrack.value?.id?.minus(1)
            else
                trackList.last().id

        _currentTrack.value = trackList.find { track ->
            track.id == prevId
        }
        prepareNewTrack()
    }

    fun playNextTrack() {
        val nextId =
            if (_currentTrack.value?.id != trackList.last().id)
                _currentTrack.value?.id?.plus(1)
            else trackList.first().id

        _currentTrack.value = trackList.find { track ->
            track.id == nextId
        }
        prepareNewTrack()
    }

    private fun prepareNewTrack() {
        mediaPlayer.stop()
        mediaPlayer.release()
        _currentPosition.postValue(0)

        mediaPlayer = repository.mediaPlayerInstance(_currentTrack.value!!.trackUri)
        mediaPlayer.setOnBufferingUpdateListener { _, progress ->
            _loadProgress.postValue(mediaPlayer.duration * progress / 100L)
        }

        if (_musicIsPlaying.value == true)
            mediaPlayer.start()

    }

    fun seekBarWasTouched(currentPosition: Int, maxPosition: Int) {
        val position = if (currentPosition <= maxPosition)
            currentPosition
        else
            maxPosition

        mediaPlayer.seekTo(position)
    }
    //endregion

    class Factory(private val repository: Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainActivityViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
