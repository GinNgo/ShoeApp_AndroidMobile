package service.serviceImplement

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Address
import repository.repositoryImplement.AddressRepository

class AddressService(
    private val repository: AddressRepository = AddressRepository()
) {
    suspend fun getAllAddresses(userId: String): List<Address> = withContext(Dispatchers.IO) {
        try {
            repository.getAllAddresses(userId)
        } catch (e: Exception) { e.printStackTrace(); emptyList() }
    }

    suspend fun addAddress(userId: String, address: Address): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.addAddress(userId, address); true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    suspend fun updateAddress(userId: String, address: Address): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.updateAddress(userId, address); true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    suspend fun deleteAddress(userId: String, addressId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.deleteAddress(userId, addressId); true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    suspend fun setPrimaryAddress(userId: String, addressId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.setPrimaryAddress(userId, addressId); true
        } catch (e: Exception) { e.printStackTrace(); false }
    }
}