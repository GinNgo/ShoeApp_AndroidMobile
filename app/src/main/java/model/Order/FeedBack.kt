package model

import com.google.firebase.Timestamp

/**
 * ⭐️ (SỬA) Thêm giá trị mặc định cho TẤT CẢ các trường.
 * Điều này sẽ tự động tạo ra hàm khởi tạo không tham số (no-argument constructor)
 * mà Firestore cần để chạy 'toObject()'.
 */
data class FeedBack(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val orderId: String = "",
    val rating: Double = 0.0,
    val review: String = "",
    val createdAt: Timestamp? = null // 👈 'null' cũng là một giá trị mặc định
)