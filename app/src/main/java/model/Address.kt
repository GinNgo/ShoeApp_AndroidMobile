package model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Address(
    val id: String = "",
    val fullName: String = "",        // Tên người nhận
    val phoneNumber: String = "",     // SĐT người nhận
    val streetAddress: String = "",   // Số nhà, tên đường, phường/xã
    val city: String = "",            // Tỉnh/Thành phố
    val country: String = "Việt Nam", // (Có thể mặc định)
    @get:PropertyName("isPrimaryShipping")
    @set:PropertyName("isPrimaryShipping")
    var isPrimaryShipping: Boolean = false // 👈 Quan trọng: Đây có phải địa chỉ chính không?
) : Serializable {

    // Hàm tiện ích để hiển thị địa chỉ đầy đủ
    fun getFullAddressString(): String {
        return "$streetAddress, $city, $country"
    }
}