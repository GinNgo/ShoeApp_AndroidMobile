package service.serviceImplement

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import model.User
import repository.IUserRepository // ⭐️ (SỬA)
import repository.repositoryImplement.UserRepository // ⭐️ (SỬA)
import service.IUserService // ⭐️ (SỬA)
import java.util.UUID

// ⭐️ (SỬA) Implement Interface và Inject IUserRepository
class UserService(
    private val repository: IUserRepository = UserRepository()
) : IUserService {

    // ⭐️ (SỬA) Thêm @Override
    override suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        // (Bạn chưa có hàm này trong Repo, nhưng nên có)
        // repository.getAllUsers()
        emptyList() // Tạm thời
    }

    override suspend fun addUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. ⭐️ (THÊM) Kiểm tra xem email đã tồn tại chưa
            val existingUser = repository.getUserByEmail(user.email)

            if (existingUser != null) {
                // 2. ⭐️ Nếu email đã tồn tại -> Báo thất bại (false)
                Log.w("UserService", "Email ${user.email} đã tồn tại.")
                return@withContext false
            }

            // 3. ⭐️ Nếu email chưa tồn tại -> Thêm user mới
            return@withContext repository.addUser(user) // Repository đã trả về Boolean

        } catch (e: Exception) {
            Log.e("UserService", "Lỗi khi thêm user: ${e.message}", e)
            return@withContext false
        }
    }

    override suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        repository.getUserByEmail(email)
    }

    override suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        repository.getUserById(id)
    }

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            repository.updateUser(userId, updates)
            true
        } catch (e: Exception) {
            Log.e("UserService", "Lỗi khi cập nhật user: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteUser(id: String): Boolean = withContext(Dispatchers.IO) {
        repository.deleteUser(id)
    }

    // ⭐️ (SỬA) uploadAvatar không dùng FirestoreBase, nó giữ nguyên
    override suspend fun uploadAvatar(userId: String, imageUri: Uri): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val storageRef = FirebaseStorage.getInstance().reference
            val avatarRef = storageRef.child("avatars/$userId/${UUID.randomUUID()}.jpg")
            val uploadTask = avatarRef.putFile(imageUri).await()
            val downloadUrl = avatarRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e("UserService", "Lỗi khi tải ảnh đại diện lên: ${e.message}", e)
            null
        }
    }
}