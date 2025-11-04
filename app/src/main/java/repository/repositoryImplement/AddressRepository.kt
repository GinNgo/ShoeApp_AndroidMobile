package repository.repositoryImplement

import android.util.Log
import data.FirestoreBase
import model.Address

class AddressRepository(
    private val firestore: FirestoreBase = FirestoreBase()
) {
    // Đường dẫn đến sub-collection
    private fun getCollectionPath(userId: String) = "users/$userId/addresses"

    suspend fun getAllAddresses(userId: String): List<Address> {
        val docs = firestore.getAll(getCollectionPath(userId))
        return docs.mapNotNull { doc ->
            doc.toObject(Address::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun addAddress(userId: String, address: Address) {
        val data = address.toHashMap()
        firestore.addData(getCollectionPath(userId), data)
    }

    suspend fun updateAddress(userId: String, address: Address) {
        val data = address.toHashMap()
        firestore.updateData(getCollectionPath(userId), address.id, data)
    }

    suspend fun deleteAddress(userId: String, addressId: String) {
        firestore.deleteData(getCollectionPath(userId), addressId)
    }

    /**
     * ⭐️ Đây là logic quan trọng:
     * Đặt 1 địa chỉ làm mặc định, và bỏ mặc định tất cả các địa chỉ cũ.
     */
    suspend fun setPrimaryAddress(userId: String, newPrimaryAddressId: String) {
        val collectionPath = getCollectionPath(userId)

        // 1. Lấy tất cả địa chỉ hiện tại (chỉ một lần)
        val allAddresses = getAllAddresses(userId)

        // 2. Bắt đầu một lệnh gộp
        firestore.runBatch { batch -> // 👈 Sẽ gọi hàm runBatch ta vừa thêm

            // 3. Vòng lặp 1: Tìm BẤT KỲ địa chỉ nào đang là TRUE và set về FALSE
            for (address in allAddresses) {
                Log.d("AddressRepository", "Address ID: ${address}")
                // Chỉ update nếu nó đang là 'true' và nó KHÔNG PHẢI là cái ta muốn set
                if (address.isPrimaryShipping == true && address.id != newPrimaryAddressId) {
                    val docRef = firestore.getDocRef(collectionPath, address.id)
                    batch.update(docRef, "isPrimaryShipping", false)
                }
            }

            // 4. Lệnh 2: Set địa chỉ MỚI (newPrimaryAddressId) thành TRUE
            val newDocRef = firestore.getDocRef(collectionPath, newPrimaryAddressId)
            batch.update(newDocRef, "isPrimaryShipping", true)
        }
    }

    private fun Address.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "fullName" to fullName,
            "phoneNumber" to phoneNumber,
            "streetAddress" to streetAddress,
            "city" to city,
            "country" to country,
            "isPrimaryShipping" to isPrimaryShipping
        )
    }
}