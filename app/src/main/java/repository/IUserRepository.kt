package repository

import model.User

interface IUserRepository {
    suspend fun addUser(user: User): Boolean
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserById(id: String): User?
    suspend fun updateUser(userId: String, updates: Map<String, Any?>): Boolean
    suspend fun deleteUser(id: String): Boolean
}