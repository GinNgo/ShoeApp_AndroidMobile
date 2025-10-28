package model

import java.io.Serializable
import java.util.Date

data class Brand(
    val id: String = "",
    val name: String = "",
    val createdAt: Date? = null,
    val description: String? = null
) : Serializable {
    override fun toString(): String {
        return name // 👈 Để hiển thị tên trong spinner (nếu cần)
    }
}