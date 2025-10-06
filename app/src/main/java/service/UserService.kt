package service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.User
import repository.UserRepository

class UserService(
    private val repository: UserRepository = UserRepository()
) {

    // 🟢 Lấy tất cả user
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        repository.getAllUsers()
    }

    // 🟢 Thêm user mới
    suspend fun addUser(user: User): String = withContext(Dispatchers.IO) {
        repository.addUser(user)
    }

    // 🟢 Lấy user theo email
    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        repository.getUserByEmail(email)
    }

    // 🟢 Lấy user theo thuộc tính bất kỳ
    suspend fun getUserByAny(property: String, value: Any): User? = withContext(Dispatchers.IO) {
        repository.getUserByAny(property, value)
    }

    // 🟢 Cập nhật user
    suspend fun updateUser(id: String, updates: Map<String, Any?>) = withContext(Dispatchers.IO) {
        repository.updateUser(id, updates)
    }

    // 🟢 Xoá user
    suspend fun deleteUser(id: String) = withContext(Dispatchers.IO) {
        repository.deleteUser(id)
    }

    // 🟢 Lọc user theo vai trò (ví dụ: "admin", "customer")
    suspend fun filterByRole(role: String): List<User> = withContext(Dispatchers.IO) {
        val allUsers = repository.getAllUsers()
        allUsers.filter { it.role.equals(role) }
    }

    // 🟢 Tính số lượng user theo vai trò
    suspend fun countUsersByRole(role: String): Int = withContext(Dispatchers.IO) {
        repository.getAllUsers().count { it.role.equals(role) }
    }
}
