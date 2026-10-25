// Neont Messenger - AuthRepository.kt
// Mengatur autentikasi Firebase + sinkronisasi profil user ke Realtime Database dan users.json
package com.neont.messenger.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.neont.messenger.data.file.FileDatabaseManager
import com.neont.messenger.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val fileDb: FileDatabaseManager
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { currentAuth ->
            trySend(currentAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun register(
        email: String,
        pass: String,
        displayName: String,
        username: String
    ): AuthResult<User> = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw IllegalStateException("Pendaftaran gagal")

            val newUser = User(
                uid = firebaseUser.uid,
                phoneNumber = email,
                username = username.replace("@", ""),
                firstName = displayName,
                lastName = "",
                bio = "Hey there! I am using Neont.",
                avatarUrl = "",
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )

            // 1. Simpan ke internal storage users.json
            fileDb.upsertUser(newUser)

            // 2. Simpan ke Firebase Realtime Database
            database.getReference("users").child(newUser.uid).setValue(newUser).await()

            AuthResult.Success(newUser)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Pendaftaran gagal")
        }
    }

    suspend fun login(email: String, pass: String): AuthResult<User> = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw IllegalStateException("Login gagal")

            // Ambil profil dari Firebase atau local cache
            val snapshot = database.getReference("users").child(firebaseUser.uid).get().await()
            val user = snapshot.getValue(User::class.java) ?: User(
                uid = firebaseUser.uid,
                phoneNumber = email,
                username = email.substringBefore("@"),
                firstName = email.substringBefore("@"),
                isOnline = true
            )

            fileDb.upsertUser(user)
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Email atau password salah")
        }
    }

    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        bio: String,
        avatarUrl: String
    ): AuthResult<User> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext AuthResult.Error("Belum login")
        try {
            val existing = fileDb.readUsers().find { it.uid == uid } ?: User(uid = uid)
            val updated = existing.copy(
                firstName = firstName,
                lastName = lastName,
                bio = bio,
                avatarUrl = avatarUrl
            )

            fileDb.upsertUser(updated)
            database.getReference("users").child(uid).setValue(updated).await()
            AuthResult.Success(updated)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Gagal memperbarui profil")
        }
    }

    suspend fun getCurrentUserProfile(): User? = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext null
        val cached = fileDb.readUsers().find { it.uid == uid }
        if (cached != null) return@withContext cached

        try {
            val snap = database.getReference("users").child(uid).get().await()
            val remote = snap.getValue(User::class.java)
            if (remote != null) {
                fileDb.upsertUser(remote)
                return@withContext remote
            }
        } catch (_: Exception) {}
        return@withContext null
    }

    fun logout() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.getReference("presence").child(uid).setValue(mapOf("isOnline" to false))
        }
        auth.signOut()
    }
}
