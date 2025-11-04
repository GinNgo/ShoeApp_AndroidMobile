package service.serviceImplement

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Voucher
import repository.repositoryImplement.VoucherRepository

class VoucherService(
    private val repository: VoucherRepository = VoucherRepository()
) {
    suspend fun getAllVouchers(): List<Voucher> = withContext(Dispatchers.IO) {
        try {
            repository.getAllVouchers()
        } catch (e: Exception) { e.printStackTrace(); emptyList() }
    }

    suspend fun addVoucher(voucher: Voucher): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.addVoucher(voucher); true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    suspend fun updateVoucher(voucher: Voucher): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.updateVoucher(voucher); true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    suspend fun deleteVoucher(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.deleteVoucher(id); true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    suspend fun getSizeVoucher(): Int = withContext(Dispatchers.IO) {
        try {
            repository.getSizeVoucher()
        } catch (e: Exception) { e.printStackTrace(); 0 }
    }
    /**
     * ⭐️ (THÊM HÀM MỚI)
     */
    suspend fun getVoucherByCode(code: String): Voucher? = withContext(Dispatchers.IO) {
        try {
            repository.getVoucherByCode(code)
        } catch (e: Exception) { e.printStackTrace(); null }
    }
}