package com.yaromchikv.musicplayer.ui.fragments

import android.os.Bundle
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
import com.yaromchikv.musicplayer.utils.Event
import com.yaromchikv.musicplayer.utils.Resource
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
    lateinit var songListAdapter: SongListAdapter

    @Inject
    lateinit var glide: RequestManager

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
                    binding.bottomBar.isVisible = true
                    result.data?.let { songs ->
                        songListAdapter.songs = songs
                    }
                }
                Status.LOADING -> {
                    binding.progressBar?.isVisible = true
                    binding.bottomBar.isVisible = false
                }
                Status.ERROR -> Unit
            }
        })

        mainViewModel.currentPlayingSong.observe(viewLifecycleOwner, {
            if (it != null) {
                val currentSong = it.toSong()
                glide.load(currentSong?.bitmapUri).into(binding.currentSongImage)
                binding.currentSongTitle.text = currentSong?.title
                binding.currentSongArtist.text = currentSong?.artist

                songListAdapter.currentPlayingSongId = currentSong?.id ?: 0
            }
        })

        mainViewModel.playbackState.observe(viewLifecycleOwner, {
            binding.playPauseButton.setImageResource(
                if (it?.isPlaying == true) R.drawable.ic_pause_circle
                else R.drawable.ic_play_circle
            )
        })

        mainViewModel.isConnected.observe(viewLifecycleOwner, { unknownError(it) })
        mainViewModel.networkError.observe(viewLifecycleOwner, { unknownError(it) })
    }

    private fun unknownError(it: Event<Resource<Boolean>>?) {
        it?.getContentForHandled()?.let { result ->
            when (result.status) {
                Status.ERROR -> Snackbar.make(
                    binding.root,
                    result.message ?: getString(R.string.unknown_error_message),
                    Snackbar.LENGTH_LONG
                ).show()
                else -> Unit
            }
        }
    }
}
