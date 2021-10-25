package com.yaromchikv.musicplayer.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.yaromchikv.musicplayer.R
import com.yaromchikv.musicplayer.databinding.FragmentHomeBinding
import com.yaromchikv.musicplayer.ui.adapters.SongListAdapter
import com.yaromchikv.musicplayer.ui.viewmodels.MainViewModel
import com.yaromchikv.musicplayer.utils.Status
import com.yaromchikv.musicplayer.utils.isPlaying
import com.yaromchikv.musicplayer.utils.toSong
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var songListAdapter: SongListAdapter

    private var playbackState: PlaybackStateCompat? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        subscribeToObservers()

        songListAdapter.setOnItemClickListener { mainViewModel.playOrToggleSong(it) }

        binding.bottomBar.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionListFragmentToSongFragment())
        }

        binding.playPauseButton.setOnClickListener {
            val currentSong = mainViewModel.currentPlayingSong.value?.toSong()
            currentSong?.let {
                mainViewModel.playOrToggleSong(currentSong, true)
            }
        }
    }

    private fun setupRecyclerView() {
        val manager = LinearLayoutManager(requireContext())
        binding.recyclerView.apply {
            adapter = songListAdapter
            layoutManager = manager
            addItemDecoration(DividerItemDecoration(requireContext(), manager.orientation))
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner, { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    binding.progressBar?.isVisible = false
                    result.data?.let { songs ->
                        songListAdapter.songs = songs
                    }
                }
                Status.LOADING -> binding.progressBar?.isVisible = true
                Status.ERROR -> Unit
            }
        })

        mainViewModel.currentPlayingSong.observe(viewLifecycleOwner, {
            if (it != null) {
                val currentSong = it.toSong()
                glide.load(currentSong?.bitmapUri).into(binding.currentSongImage)
                binding.currentSongTitle.text = currentSong?.title
                binding.currentSongArtist.text = currentSong?.artist
            }
        })

        mainViewModel.playbackState.observe(viewLifecycleOwner, {
            playbackState = it
            binding.playPauseButton.setImageResource(
                if (playbackState?.isPlaying == true)
                    R.drawable.ic_pause_circle
                else
                    R.drawable.ic_play_circle
            )
        })

        mainViewModel.isConnected.observe(viewLifecycleOwner, {
            it?.getContentForHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> Snackbar.make(
                        binding.root,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        })

        mainViewModel.networkError.observe(viewLifecycleOwner, {
            it?.getContentForHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> Snackbar.make(
                        binding.root,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        })
    }
}





















