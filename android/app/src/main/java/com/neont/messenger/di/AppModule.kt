// Neont Messenger - AppModule
// Hilt Application Module untuk Gson, FileDatabaseManager, dan Repositories
package com.neont.messenger.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.neont.messenger.data.file.FileDatabaseManager
import com.neont.messenger.data.firebase.AuthRepo
import com.neont.messenger.data.repository.ChatRepository
import com.neont.messenger.data.repository.PresenceRepository
import com.neont.messenger.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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

    @Provides
    @Singleton
    fun provideChatRepository(
        database: FirebaseDatabase,
        fileDb: FileDatabaseManager
    ): ChatRepository = ChatRepository(database, fileDb)

    @Provides
    @Singleton
    fun providePresenceRepository(
        database: FirebaseDatabase
    ): PresenceRepository = PresenceRepository(database)

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase,
        fileDb: FileDatabaseManager
    ): UserRepository = UserRepository(auth, database, fileDb)

    @Provides
    @Singleton
    fun provideAuthRepo(
        auth: FirebaseAuth,
        fileDb: FileDatabaseManager
    ): AuthRepo = AuthRepo(auth, fileDb)
}
