package com.yaromchikv.musicplayer.player

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.yaromchikv.musicplayer.R
import com.yaromchikv.musicplayer.utils.Constants.NOTIF_CHANNEL_ID
import com.yaromchikv.musicplayer.utils.Constants.NOTIF_ID

class MusicNotificationManager(
    private val musicService: MusicService,
    sessionToken: MediaSessionCompat.Token,
    private var newSongCallback: () -> Unit
) {

    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(musicService, sessionToken)
        notificationManager =
            PlayerNotificationManager.Builder(musicService, NOTIF_ID, NOTIF_CHANNEL_ID)
                .setChannelNameResourceId(R.string.notification_channel_name)
                .setChannelDescriptionResourceId(R.string.notification_channel_description)
                .setSmallIconResourceId(R.drawable.music_placeholder)
                .setNotificationListener(MusicPlayerNotificationListener())
                .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
                .build().apply {
                    setMediaSessionToken(sessionToken)
                }
    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class MusicPlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            super.onNotificationPosted(notificationId, notification, ongoing)
            musicService.apply {
                if (ongoing && !isForegroundService) {
                    ContextCompat.startForegroundService(
                        this,
                        Intent(applicationContext, this::class.java)
                    )
                    startForeground(NOTIF_ID, notification)
                    isForegroundService = true
                }
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            musicService.apply {
                stopForeground(true)
                isForegroundService = false
                stopSelf()
            }
        }
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback()
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            return mediaController.metadata.description.subtitle.toString()
        }

        private var currentBitmap: Bitmap? = null

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with(musicService)
                .asBitmap()
                .load(mediaController.metadata.description.iconUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        callback.onBitmap(resource)
                        currentBitmap = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })
            return currentBitmap
        }
    }
}