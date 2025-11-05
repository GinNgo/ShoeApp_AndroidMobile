package service

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.User

interface IUserService {
    suspend fun getAllUsers(): List<User>
    suspend fun addUser(user: User): Boolean
    suspend fun getUserByEmail(email: String): User?
    suspend fun updateUser(userId: String, updates: Map<String, Any?>): Boolean
    suspend fun deleteUser(id: String): Boolean
    suspend fun uploadAvatar(userId: String, imageUri: Uri): String?
    suspend fun getUserById(id: String): User?
}