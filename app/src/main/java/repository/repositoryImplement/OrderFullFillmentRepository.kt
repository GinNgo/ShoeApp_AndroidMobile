package repository.repositoryImplement

import com.google.firebase.Timestamp
import data.FirestoreBase
import model.OrderFullfillment
import repository.IOrderFullFillmentRepository

class OrderFullFillmentRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionName: String = "orderFullfillments"
) : IOrderFullFillmentRepository {

    override suspend fun createOrderFullFillment(orderFullfillment: OrderFullfillment) {
        val data = hashMapOf(
            "orderId" to orderFullfillment.orderId,
            "address" to orderFullfillment.address,
            "status" to orderFullfillment.status.name,
            "time" to (orderFullfillment.time ?: Timestamp.now())
        )
        firestore.addData(collectionName, data)
    }
}
