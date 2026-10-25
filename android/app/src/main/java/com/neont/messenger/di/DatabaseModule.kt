// Neont Messenger - DatabaseModule.kt
// Hilt Module untuk File-based Database & Gson
package com.neont.messenger.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.neont.messenger.data.file.FileDatabaseManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    @Provides
    @Singleton
    fun provideFileDatabaseManager(
        @ApplicationContext context: Context,
        gson: Gson
    ): FileDatabaseManager = FileDatabaseManager(context, gson)
}
