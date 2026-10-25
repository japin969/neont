// Neont Messenger - UserRepository
// Sinkronisasi data profil user antara users.json dan Firebase Realtime Database
package com.neont.messenger.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.neont.messenger.data.file.FileDatabaseManager
import com.neont.messenger.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val fileDb: FileDatabaseManager
) {
    /**
     * Membaca semua kontak dan cache user dari users.json
     */
    suspend fun getLocalUsers(): List<User> {
        return fileDb.readUsers()
    }

    /**
     * Mengambil profil user berdasarkan UID dengan fallback offline-first
     */
    suspend fun getUserProfile(uid: String): User? = withContext(Dispatchers.IO) {
        // 1. Cek cache lokal users.json terlebih dahulu
        val cached = fileDb.readUsers().find { it.uid == uid }

        // 2. Jika online, sinkronkan dari Firebase Realtime Database
        try {
            val snapshot = database.getReference("users").child(uid).get().await()
            val remoteUser = snapshot.getValue(User::class.java)
            if (remoteUser != null) {
                fileDb.upsertUser(remoteUser)
                return@withContext remoteUser
            }
        } catch (_: Exception) {}

        return@withContext cached
    }

    /**
     * Mengupdate profil pengguna lokal dan remote
     */
    suspend fun updateCurrentUserProfile(
        firstName: String,
        lastName: String,
        bio: String,
        avatarUrl: String
    ): Result<User> = withContext(Dispatchers.IO) {
        val currentFirebaseUser = auth.currentUser ?: return@withContext Result.failure(IllegalStateException("User belum login"))
        val uid = currentFirebaseUser.uid

        val existing = getUserProfile(uid) ?: User(uid = uid, phoneNumber = currentFirebaseUser.email ?: "")
        val updated = existing.copy(
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            avatarUrl = avatarUrl
        )

        // Simpan langsung ke JSON lokal
        fileDb.upsertUser(updated)

        // Push ke Firebase
        try {
            database.getReference("users").child(uid).setValue(updated).await()
            Result.success(updated)
        } catch (e: Exception) {
            // Meskipun push Firebase gagal (offline), data sudah tersimpan di lokal
            Result.success(updated)
        }
    }
}
