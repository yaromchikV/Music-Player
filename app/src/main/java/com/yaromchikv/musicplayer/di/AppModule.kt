package com.yaromchikv.musicplayer.di

import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.yaromchikv.musicplayer.R
import com.yaromchikv.musicplayer.player.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ) = MusicServiceConnection(context)

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.music_placeholder)
            .error(R.drawable.music_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.DATA)

    )
}