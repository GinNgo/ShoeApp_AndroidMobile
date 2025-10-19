package repository

import data.FirestoreBase
import kotlinx.coroutines.tasks.await
import model.Category

class CategoryRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionName: String = "categories"
) {
    // 🟢 Lấy tất cả danh mục
    suspend fun getAllCategories(): List<Category> {
        val docs = firestore.getAll(collectionName)
        return docs.mapNotNull { doc ->
            val category = doc.toObject(Category::class.java)
            category?.copy(id = doc.id)
        }
    }

    // 🟢 Lấy danh mục theo ID
    suspend fun getCategoryById(id: String): Category? {
        val doc = firestore.getById(collectionName, id)
        return doc?.toObject(Category::class.java)?.copy(id = doc.id)
    }

    // 🟢 Thêm danh mục
    suspend fun addCategory(category: Category) {
        val data = hashMapOf(
            "name" to category.name,
            "description" to category.description,
            "createdAt" to (category.createdAt ?: com.google.firebase.Timestamp.now()),
        )
        firestore.addData(collectionName, data)
    }

    // 🟢 Cập nhật danh mục
    suspend fun updateCategory(category: Category) {
        val data = hashMapOf(
            "name" to category.name,
            "description" to category.description
        )
        firestore.updateData(collectionName, category.id, data)
    }

    // 🟢 Xóa danh mục
    suspend fun deleteCategory(id: String) {
        firestore.deleteData(collectionName, id)
    }

    // 🟡 Đếm tổng danh mục
    suspend fun getSizeCategory(): Int {
        val docs = firestore.getAll(collectionName)
        return docs.size
    }
}
