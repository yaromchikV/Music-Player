package com.yaromchikv.musicplayer.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.yaromchikv.musicplayer.R
import com.yaromchikv.musicplayer.data.Song
import com.yaromchikv.musicplayer.databinding.ListItemBinding
import javax.inject.Inject

class SongListAdapter @Inject constructor(
    private val context: Context,
    private val glide: RequestManager
) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    var currentPlayingSongId: Int = 0

    private var onItemClickListener: ((Song) -> Unit)? = null
    fun setOnItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }

    inner class SongViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            with(binding) {
                songTitle.text = song.title
                songArtist.text = song.artist
                glide.load(song.bitmapUri).into(songImage)

                val titleTextColor =
                    if (song.id == currentPlayingSongId) R.color.pink else R.color.black
                val artistTextColor =
                    if (song.id == currentPlayingSongId) R.color.pink else R.color.default_text_color

                songTitle.setTextColor(ContextCompat.getColor(context, titleTextColor))
                songArtist.setTextColor(ContextCompat.getColor(context, artistTextColor))

                listItem.setOnClickListener {
                    onItemClickListener?.let { click ->
                        currentPlayingSongId = song.id
                        notifyDataSetChanged()
                        click(song)
                    }
                }
            }
        }
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
