package repository.repositoryImplement

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot // ⭐️ (THÊM)
import data.FirestoreBase
import model.Order
import model.OrderItem // ⭐️ (THÊM)
import model.OrderStatus
import repository.IOrderRepository

class OrderRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionPath: String = "orders"
) : IOrderRepository {

    /**
     * ⭐️ (MỚI) HÀM HELPER ĐỂ ĐỌC ORDER BẰNG TAY
     * Hàm này sẽ đọc DocumentSnapshot và tự build object Order
     * để đảm bảo 'items' và 'isReviewed' được phân tích (parse) chính xác.
     */
    private fun DocumentSnapshot.toOrder(): Order? {
        try {
            // 1. Lấy các trường đơn giản (tự động)
            val order = this.toObject(Order::class.java) ?: return null

            // 2. Phân tích (parse) 'items' (List<Map<String, Any>>) bằng tay
            val itemsData = this.get("items") as? List<Map<String, Any>> ?: emptyList()

            val parsedItems = itemsData.map { itemMap ->
                OrderItem(
                    productId = itemMap["productId"] as? String ?: "",
                    productName = itemMap["productName"] as? String ?: "",
                    productImage = itemMap["productImage"] as? String ?: "",
                    selectedColor = itemMap["selectedColor"] as? String ?: "",
                    selectedSize = itemMap["selectedSize"] as? String ?: "",
                    quantity = (itemMap["quantity"] as? Long)?.toInt() ?: 0,
                    unitPrice = itemMap["unitPrice"] as? Double ?: 0.0,

                    // ⭐️⭐️ ĐÂY LÀ DÒNG SỬA LỖI CỦA BẠN ⭐️⭐️
                    // Nó sẽ đọc 'isReviewed' từ DB (là true)
                    // Nếu không tìm thấy, nó mới là 'false'
                    isReviewed = itemMap["isReviewed"] as? Boolean ?: false
                )
            }

            // 3. Trả về Order hoàn chỉnh với 'items' đã được parse đúng
            return order.copy(
                id = this.id,
                items = parsedItems // 👈 Ghi đè list 'items'
            )

        } catch (e: Exception) {
            Log.e("OrderRepository", "Lỗi khi phân tích (parse) order: ${this.id}", e)
            return null
        }
    }

    // (Hàm createOrder giữ nguyên)
    override suspend fun createOrder(order: Order): Boolean {
        return try {
            firestore.addData(collectionPath, order.toHashMap())
            true
        } catch (e: Exception) {
            Log.e("OrderRepository", "Lỗi tạo Order: ${e.message}", e)
            false
        }
    }

    // (Hàm updateOrder giữ nguyên)
    override suspend fun updateOrder(order: Order): Boolean {
        return try {
            val data = order.toHashMap() // Dùng helper
            firestore.updateData(collectionPath, order.id, data)
            true
        } catch (e: Exception) {
            Log.e("OrderRepository", "Lỗi update Order: ${e.message}", e)
            false
        }
    }

    // ⭐️ (SỬA) Lấy tất cả đơn hàng, dùng helper 'toOrder'
    override suspend fun getAllOrders(userId: String): List<Order> {
        return try {
            val docs = firestore.getListBy(collectionPath, "userId", userId)
            docs.mapNotNull { it.toOrder() } // 👈 Sửa ở đây
        } catch (e: Exception) {
            Log.e("OrderRepository", "Lỗi lấy Orders: ${e.message}", e)
            emptyList()
        }
    }

    // ⭐️ (SỬA) Lấy đơn theo status, dùng helper 'toOrder'
    override suspend fun getOrdersByStatus(userId: String, status: OrderStatus): List<Order> {
        return try {
            val conditions = mapOf(
                "userId" to userId,
                "status" to status.name // Lưu Enum dưới dạng String
            )
            val docs = firestore.getDataWhere(collectionPath, conditions)
            docs.mapNotNull { it.toOrder() } // 👈 Sửa ở đây
        } catch (e: Exception) {
            Log.e("OrderRepository", "Lỗi lấy Orders: ${e.message}", e)
            emptyList()
        }
    }

    // (Hàm cancelOrder giữ nguyên)
    override suspend fun cancelOrder(orderId: String): Boolean {
        return try {
            firestore.updateData(collectionPath, orderId, mapOf("status" to OrderStatus.CANCELLED.name))
            true
        } catch (e: Exception) {
            Log.e("OrderRepository", "Lỗi hủy đơn: ${e.message}", e)
            false
        }
    }

    // ⭐️ (SỬA) Hàm toHashMap (Đã đúng, chỉ cần đảm bảo 'isReviewed' có ở đây)
    private fun Order.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "userId" to userId,
            "createdAt" to createdAt,
            "status" to status.name,
            "totalAmount" to totalAmount,
            "shippingAddress" to shippingAddress,
            "discountCode" to discountCode,
            "discountAmount" to discountAmount,

            "items" to items.map { item ->
                hashMapOf(
                    "productId" to item.productId,
                    "productName" to item.productName,
                    "productImage" to item.productImage,
                    "selectedColor" to item.selectedColor,
                    "selectedSize" to item.selectedSize,
                    "quantity" to item.quantity,
                    "unitPrice" to item.unitPrice,
                    "isReviewed" to item.isReviewed // ⭐️ Đảm bảo lưu 'isReviewed'
                )
            }
        )
    }
}