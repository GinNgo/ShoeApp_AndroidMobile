package model

import android.content.Context
import com.example.shoesapp.R
import com.google.firebase.Timestamp

data class                                                                                                                      User(
    val id: String? = "",
    val username: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val createdAt: Timestamp? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val billingAddress: String? = null,
    val phoneNumber: String? = null,
    val role: Int = 0,
    val gender: String? = null,
    val date: Timestamp? = null,
    val avatarUrl: String? = null // ⭐️ Trường này sẽ lưu đường dẫn file (vd: /data/.../avatar.jpg)
) {
    /**
     * ⭐️ (XÓA) Xóa hàm getAvatarImageResId()
     * Chúng ta sẽ xử lý logic tải ảnh trong Activity bằng Glide
     */
}
