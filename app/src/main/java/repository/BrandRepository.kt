package repository

import data.FirestoreBase
import model.Brand

class BrandRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionName: String = "brands" // 👈 Đổi collection
) {
    // 🟢 Lấy tất cả thương hiệu
    suspend fun getAllBrands(): List<Brand> {
        val docs = firestore.getAll(collectionName)
        return docs.mapNotNull { doc ->
            val brand = doc.toObject(Brand::class.java)
            brand?.copy(id = doc.id)
        }
    }

    // 🟢 Lấy thương hiệu theo ID
    suspend fun getBrandById(id: String): Brand? {
        val doc = firestore.getById(collectionName, id)
        return doc?.toObject(Brand::class.java)?.copy(id = doc.id)
    }

    // 🟢 Thêm thương hiệu
    suspend fun addBrand(brand: Brand) {
        val data = hashMapOf(
            "name" to brand.name,
            "description" to brand.description,
            "createdAt" to (brand.createdAt ?: com.google.firebase.Timestamp.now()),
        )
        firestore.addData(collectionName, data)
    }

    // 🟢 Cập nhật thương hiệu
    suspend fun updateBrand(brand: Brand) {
        val data = hashMapOf(
            "name" to brand.name,
            "description" to brand.description
        )
        firestore.updateData(collectionName, brand.id, data)
    }

    // 🟢 Xóa thương hiệu
    suspend fun deleteBrand(id: String) {
        firestore.deleteData(collectionName, id)
    }

    // 🟡 Đếm tổng thương hiệu
    suspend fun getSizeBrand(): Int {
        val docs = firestore.getAll(collectionName)
        return docs.size
    }
}