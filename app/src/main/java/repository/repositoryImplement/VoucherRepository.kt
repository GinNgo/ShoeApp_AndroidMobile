package repository.repositoryImplement

import android.util.Log
import com.google.firebase.Timestamp
import data.FirestoreBase
import model.Voucher

class VoucherRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionName: String = "vouchers"
) {
    // 🟢 Lấy tất cả
    suspend fun getAllVouchers(): List<Voucher> {
        val docs = firestore.getAll(collectionName)
        return docs.mapNotNull { doc ->
            doc.toObject(Voucher::class.java)?.copy(id = doc.id)
        }
    }

    // 🟢 Thêm mới
    suspend fun addVoucher(voucher: Voucher) {
        val data = voucher.toHashMap()
        firestore.addData(collectionName, data)
    }

    // 🟢 Cập nhật
    suspend fun updateVoucher(voucher: Voucher) {
        val data = voucher.toHashMap()
        firestore.updateData(collectionName, voucher.id, data)
    }

    // 🟢 Xóa
    suspend fun deleteVoucher(id: String) {
        firestore.deleteData(collectionName, id)
    }

    // 🟡 Đếm
    suspend fun getSizeVoucher(): Int {
        val docs = firestore.getAll(collectionName)
        return docs.size
    }
    /**
     * ⭐️ (THÊM HÀM MỚI)
     * Lấy voucher theo mã code (vd: "SALE100K")
     */
    suspend fun getVoucherByCode(code: String): Voucher? {
        return try {
            val doc = firestore.getSingleBy(collectionName, "code", code)
            doc?.toObject(Voucher::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("VoucherRepository", "Lỗi lấy voucher: ${e.message}")
            null
        }
    }
    // Tiện ích chuyển đổi sang HashMap để lưu
    private fun Voucher.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "code" to code,
            "description" to description,
            "discountType" to discountType.name, // Lưu Enum dưới dạng String
            "discountValue" to discountValue,
            "minOrderValue" to minOrderValue,
            "maxDiscountAmount" to maxDiscountAmount,
            "expirationDate" to expirationDate?.let { Timestamp(it) },
            "usageLimit" to usageLimit,
            "usageCount" to usageCount,
            "isActive" to isActive,
            "createdAt" to (createdAt ?: Timestamp.now())
        )
    }
}