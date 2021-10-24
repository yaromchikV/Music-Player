package com.yaromchikv.musicplayer.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.yaromchikv.musicplayer.data.Song
import com.yaromchikv.musicplayer.databinding.ListItemBinding
import javax.inject.Inject

class SongListAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    inner class SongViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.songTitle.text = song.title
            binding.songArtist.text = song.artist
            glide.load(song.bitmapUri).into(binding.songImage)

            binding.listItem.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

    private var onItemClickListener: ((Song) -> Unit)? = null

    fun setOnItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(layoutInflater, parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }
}




















