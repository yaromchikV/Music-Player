package com.yaromchikv.musicplayer.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.RequestManager
import com.yaromchikv.musicplayer.R
import com.yaromchikv.musicplayer.data.Song
import com.yaromchikv.musicplayer.databinding.FragmentSongBinding
import com.yaromchikv.musicplayer.ui.viewmodels.MainViewModel
import com.yaromchikv.musicplayer.ui.viewmodels.SongViewModel
import com.yaromchikv.musicplayer.utils.Status
import com.yaromchikv.musicplayer.utils.isPlaying
import com.yaromchikv.musicplayer.utils.toSong
import com.yaromchikv.musicplayer.utils.toTimeFormat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    private val binding: FragmentSongBinding by viewBinding()

    private val mainViewModel: MainViewModel by viewModels()
    private val songViewModel: SongViewModel by viewModels()

    private var currentPlayingSong: Song? = null

    private var shouldUpdateSeekbar = true

    @Inject
    lateinit var glide: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        with(binding) {
            playPauseButton.setOnClickListener {
                currentPlayingSong?.let {
                    mainViewModel.playOrToggleSong(it, true)
                }
            }

            prevButton.setOnClickListener {
                mainViewModel.skipToPreviousSong()
            }

            nextButton.setOnClickListener {
                mainViewModel.skipToNextSong()
            }

            backButton.setOnClickListener {
                findNavController().popBackStack()
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        binding.currentTime.text = progress.toLong().toTimeFormat()
                    } else {
                        binding.seekBar.progress =
                            songViewModel.currentPlayerPosition.value?.toInt() ?: 0
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    shouldUpdateSeekbar = false
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.let {
                        mainViewModel.seekTo(it.progress.toLong())
                    }
                    shouldUpdateSeekbar = true
                }
            })
        }
    }

    private fun updateSongData(song: Song) {
        binding.songTitle.text = song.title
        binding.songArtist.text = song.artist
        glide.load(song.bitmapUri).into(binding.songImage)
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner, {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if (currentPlayingSong == null && songs.isNotEmpty()) {
                                currentPlayingSong = songs[0]
                                updateSongData(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        })

        mainViewModel.currentPlayingSong.observe(viewLifecycleOwner, {
            if (it != null) {
                currentPlayingSong = it.toSong()
                updateSongData(requireNotNull(currentPlayingSong))
            }
        })

        mainViewModel.playbackState.observe(viewLifecycleOwner, {
            binding.playPauseButton.setImageResource(
                if (it?.isPlaying == true)
                    R.drawable.ic_pause_circle
                else
                    R.drawable.ic_play_circle
            )
        })

        songViewModel.currentPlayerPosition.observe(viewLifecycleOwner, {
            if (shouldUpdateSeekbar) {
                binding.seekBar.progress = it.toInt()
                binding.currentTime.text = it.toTimeFormat()
            }
        })

        songViewModel.currentSongDuration.observe(viewLifecycleOwner, {
            binding.seekBar.max = it.toInt()
            binding.duration.text = it.toTimeFormat()
        })
    }
}
