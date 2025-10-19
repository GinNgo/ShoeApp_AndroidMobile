package repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import model.User

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("users")

    // 🟢 Thêm user mới
    suspend fun addUser(user: User): String {
        val data = mapOf(
            "username" to user.username,
            "email" to user.email,
            "passwordHash" to user.passwordHash,
            "createdAt" to (user.createdAt ?: Timestamp.now()),
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "shippingAddress" to user.shippingAddress,
            "billingAddress" to user.billingAddress,
            "phoneNumber" to user.phoneNumber,
            "role" to user.role
        )
        val docRef = collection.add(data).await()
        return docRef.id
    }

    // 🟢 Lấy toàn bộ user
    suspend fun getAllUsers(): List<User> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }
    }

    // 🟢 Lấy user theo email
    suspend fun getUserByEmail(email: String): User? {
        val snapshot = collection
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.let { doc ->
            val user = doc.toObject(User::class.java)
            user?.copy(
                id = doc.id,
                role = doc.getLong("role")?.toInt() ?: 0
            )
        }
    }

    // 🟢 Lấy user theo thuộc tính bất kỳ
    suspend fun getUserByAny(property: String, value: Any): User? {
        val snapshot = collection
            .whereEqualTo(property, value)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject(User::class.java)?.copy(
            id = snapshot.documents.firstOrNull()?.id
        )
    }

    // 🟢 Cập nhật thông tin user
    suspend fun updateUser(documentId: String, updates: Map<String, Any?>) {
        collection.document(documentId).update(updates).await()
    }

    // 🟢 Xoá user theo ID
    suspend fun deleteUser(documentId: String) {
        collection.document(documentId).delete().await()
    }

    // 🟢 Tìm danh sách user theo firstName
    suspend fun getUsersByFirstName(name: String): List<User> {
        val snapshot = collection.whereEqualTo("firstName", name).get().await()
        return snapshot.documents.mapNotNull { it.toObject(User::class.java)?.copy(id = it.id) }
    }
}
