// Neont Messenger - AuthRepo
// Firebase Authentication integrated with local JSON persistence in users.json
package com.neont.messenger.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.neont.messenger.data.file.FileDatabaseManager
import com.neont.messenger.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

@Singleton
class AuthRepo @Inject constructor(
    private val auth: FirebaseAuth,
    private val fileDb: FileDatabaseManager
) {
    /**
     * Get current authenticated Firebase user instance
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Quick check if a user is currently authenticated
     */
    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Stream real-time authentication state updates
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { currentAuth ->
            trySend(currentAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Register a new user with Email and Password
     * Also writes immediately to local storage (users.json) for offline-first resilience
     */
    suspend fun registerWithEmail(
        email: String,
        pass: String,
        displayName: String,
        username: String
    ): AuthResult<User> = try {
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        val firebaseUser = result.user ?: throw IllegalStateException("User creation failed")
        val newUser = User(
            uid = firebaseUser.uid,
            phoneNumber = email,
            username = username,
            firstName = displayName,
            lastName = "",
            bio = "Hey there! I am using Neont.",
            avatarUrl = "",
            isOnline = true,
            lastSeen = System.currentTimeMillis()
        )
        // Store locally in users.json immediately
        fileDb.upsertUser(newUser)
        AuthResult.Success(newUser)
    } catch (e: Exception) {
        AuthResult.Error(e.localizedMessage ?: "Unknown registration error")
    }

    /**
     * Log in an existing user with Email and Password
     */
    suspend fun loginWithEmail(email: String, pass: String): AuthResult<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, pass).await()
        val user = result.user ?: throw IllegalStateException("Sign in returned empty user")
        AuthResult.Success(user)
    } catch (e: Exception) {
        AuthResult.Error(e.localizedMessage ?: "Invalid email or password")
    }

    /**
     * Sign out current user
     */
    fun logout() {
        auth.signOut()
    }
}
