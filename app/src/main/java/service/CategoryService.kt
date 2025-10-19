package service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Category
import repository.CategoryRepository

class CategoryService(
    private val repository: CategoryRepository = CategoryRepository()
) {

    // 🟢 Lấy tất cả danh mục
    suspend fun getAllCategories(): List<Category> = withContext(Dispatchers.IO) {
        try {
            repository.getAllCategories()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 🟢 Lấy danh mục theo ID
    suspend fun getCategoryById(id: String): Category? = withContext(Dispatchers.IO) {
        try {
            repository.getCategoryById(id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 🟢 Thêm danh mục
    suspend fun addCategory(category: Category): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.addCategory(category)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🟢 Cập nhật danh mục
    suspend fun updateCategory(category: Category): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.updateCategory(category)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🟢 Xóa danh mục
    suspend fun deleteCategory(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.deleteCategory(id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🟡 Đếm tổng số danh mục
    suspend fun getSizeCategory(): Int = withContext(Dispatchers.IO) {
        try {
            repository.getSizeCategory()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}
