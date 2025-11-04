package service.serviceImplement

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Brand
import repository.repositoryImplement.BrandRepository

class BrandService(
    private val repository: BrandRepository = BrandRepository()
) {

    // 🟢 Lấy tất cả thương hiệu
    suspend fun getAllBrands(): List<Brand> = withContext(Dispatchers.IO) {
        try {
            repository.getAllBrands()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 🟢 Lấy thương hiệu theo ID
    suspend fun getBrandById(id: String): Brand? = withContext(Dispatchers.IO) {
        try {
            repository.getBrandById(id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 🟢 Thêm thương hiệu
    suspend fun addBrand(brand: Brand): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.addBrand(brand)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🟢 Cập nhật thương hiệu
    suspend fun updateBrand(brand: Brand): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.updateBrand(brand)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🟢 Xóa thương hiệu
    suspend fun deleteBrand(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.deleteBrand(id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🟡 Đếm tổng số thương hiệu
    suspend fun getSizeBrand(): Int = withContext(Dispatchers.IO) {
        try {
            repository.getSizeBrand()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}