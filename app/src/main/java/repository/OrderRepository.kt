package repository

import android.util.Log
import com.google.firebase.Timestamp
import data.FirestoreBase
import model.Order.Order
import model.Order.OrderStatus
import model.Product
import model.ProductImage

class OrderRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionName: String = "orders"
) : IOrderRepository {

    override suspend fun createOrder(order: Order) {
        Log.d("item",order.product.images.toString())
        val data = hashMapOf(
            "userId" to order.userId,
            "product" to order.product,
            "quantity" to order.quantity,
            "status" to order.status.name,
            "totalPrice" to order.totalPrice,
            "createdAt" to (order.createdAt ?: Timestamp.now())
        )
        firestore.addData(collectionName, data)
    }

    override suspend fun updateOrderStatus(orderId: String, orderStatus: OrderStatus) {
        val updates = hashMapOf(
            "status" to orderStatus.name,
            "updatedAt" to Timestamp.now()
        )
        firestore.updateData(collectionName, orderId, updates)
    }

    override suspend fun getAllOrderByUserIdAndStatus(
        userId: String,
        status: OrderStatus
    ): List<Order> {
        // Truy vấn Firestore theo điều kiện userId và status
        val querySnapshot = firestore.getDataWhere(
            collectionName,
            mapOf(
                "userId" to userId,
                "status" to status.name
            )
        )

        // Chuyển kết quả thành danh sách Order
        return querySnapshot.mapNotNull { doc ->
            try {
                val product = doc.get("product") as? Map<String, Any> ?: return@mapNotNull null
                // Lấy images (dạng List<Map<String, Any>>)
                val imagesList = product["images"] as? List<Map<String, Any>> ?: emptyList()

// Chuyển từng Map → ProductImage
                val images = imagesList.mapNotNull { imageMap ->
                    ProductImage(
                        imageUrl = imageMap["imageUrl"] as? String ?: "",
                        isPrimary = imageMap["isPrimary"] as? Boolean ?: false
                    )
                }
                val orderProduct = Product(
                    id = product["id"] as? String ?: "",
                    name = product["name"] as? String ?: "",
                    price = (product["price"] as? Number)?.toDouble() ?: 0.0,
                    images = images
                )
                Log.d("order", orderProduct.images.toString())
                Order(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    product = orderProduct,
                    quantity = (doc.getLong("quantity") ?: 0).toInt(),
                    status = OrderStatus.valueOf(
                        doc.getString("status") ?: OrderStatus.IN_DELIVERY.name
                    ),
                    totalPrice = (doc.getDouble("totalPrice") ?: 0.0),
                    createdAt = doc.getTimestamp("createdAt")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
