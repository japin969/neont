// Neont Messenger - FirebaseModule.kt
// Hilt Module untuk FirebaseAuth & FirebaseDatabase instances
package com.neont.messenger.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val db = FirebaseDatabase.getInstance()
        try {
            db.setPersistenceEnabled(true)
        } catch (_: Exception) {}
        return db
    }
}
