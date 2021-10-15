package com.yaromchikv.musicplayer.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.yaromchikv.musicplayer.R
import com.yaromchikv.musicplayer.databinding.ActivityMainBinding
import com.yaromchikv.musicplayer.model.Track
import com.yaromchikv.musicplayer.repository.Repository
import com.yaromchikv.musicplayer.toTimer
import com.yaromchikv.musicplayer.viewmodel.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainActivityViewModel by lazy {
        ViewModelProvider(this, MainActivityViewModel.Factory(Repository(this))).get(
            MainActivityViewModel::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
        initStateFlows()

        displayTrackData(viewModel.currentTrack.value!!)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        with(binding) {
            playPauseButton.setOnClickListener {
                viewModel.thePlayPauseButtonWasClicked()
            }

            prevButton.setOnClickListener {
                viewModel.playPrevTrack()
            }

            nextButton.setOnClickListener {
                viewModel.playNextTrack()
            }

            seekBar.setOnTouchListener { _, _ ->
                viewModel.seekBarWasTouched(
                    binding.seekBar.progress,
                    binding.seekBar.secondaryProgress
                )
                false
            }
        }
    }

    private fun initStateFlows() {
        viewModel.currentTrack.observe(this, { newTrack ->
            displayTrackData(newTrack)
        })

        viewModel.currentPosition.observe(this, { newPosition ->
            binding.seekBar.progress = newPosition
            binding.currentTime.text = newPosition.toLong().toTimer()
        })

        viewModel.loadProgress.observe(this, { newLoadProgress ->
            binding.seekBar.secondaryProgress = newLoadProgress.toInt()
        })

        viewModel.musicIsPlaying.observe(this, { newPlayingStatus ->
            if (newPlayingStatus)
                binding.playPauseButton.setImageResource(R.drawable.ic_pause_circle)
            else
                binding.playPauseButton.setImageResource(R.drawable.ic_play_circle)

        })
    }

    private fun displayTrackData(track: Track) {
        Glide.with(this)
            .load(track.bitmapUri)
            .placeholder(R.drawable.music_placeholder)
            .error(R.drawable.music_placeholder)
            .transition(withCrossFade())
            .transform(CenterCrop())
            .into(binding.trackImage)

        binding.apply {
            trackTitle.text = track.title
            trackArtist.text = track.artist

            duration.text = track.duration.toTimer()
            seekBar.max = track.duration.toInt()
        }
    }
}
