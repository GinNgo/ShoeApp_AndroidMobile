package repository.repositoryImplement

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import data.FirestoreBase // ⭐️ (THÊM)
import model.User
import repository.IUserRepository // ⭐️ (THÊM)

// ⭐️ (SỬA) Implement Interface và Inject FirestoreBase
class UserRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionPath: String = "users"
) : IUserRepository {

    // ⭐️ (SỬA) Hàm helper giữ nguyên, không cần thay đổi
    private fun DocumentSnapshot.toUser(): User? {
        return try {
            val user = this.toObject(User::class.java)
            user?.copy(id = this.id)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error converting document to User: ${e.message}", e)
            null
        }
    }

    private fun User.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "email" to this.email,
            "passwordHash" to this.passwordHash,
            "firstName" to this.firstName,
            "lastName" to this.lastName,
            "billingAddress" to this.billingAddress,
            "phoneNumber" to this.phoneNumber,
            "role" to this.role,
            "gender" to this.gender,
            "date" to this.date,
            "createdAt" to (this.createdAt ?: Timestamp.now()),
            "avatarUrl" to this.avatarUrl
        )
    }

    override suspend fun addUser(user: User): Boolean {
        return try {
            val data = user.toHashMap()
            firestore.addData(collectionPath, data) // ⭐️ (SỬA)
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding user: ${e.message}", e)
            false
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        // ⭐️ (SỬA)
        val doc = firestore.getSingleBy(collectionPath, "email", email)
        return doc?.toUser()
    }

    override suspend fun getUserById(id: String): User? {
        // ⭐️ (SỬA)
        val doc = firestore.getById(collectionPath, id)
        return doc?.toUser()
    }

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>): Boolean {
        return try {
            firestore.updateData(collectionPath, userId, updates) // ⭐️ (SỬA)
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteUser(id: String): Boolean {
        return try {
            firestore.deleteData(collectionPath, id) // ⭐️ (SỬA)
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error deleting user: ${e.message}", e)
            false
        }
    }
}