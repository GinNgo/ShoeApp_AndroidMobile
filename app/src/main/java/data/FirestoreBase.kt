package data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resumeWithException

class FirestoreBase(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // Lấy tham chiếu Document
    fun getDocRef(collectionPath: String, id: String): com.google.firebase.firestore.DocumentReference {
        return db.collection(collectionPath).document(id)
    }

    // Chạy Batch
    suspend fun runBatch(batchOperation: (WriteBatch) -> Unit) {
        val batch = db.batch()
        batchOperation(batch)
        batch.commit().await()
    }

    // 🟢 Thêm document
    suspend fun addData(
        collectionPath: String,
        data: Map<String, Any?>
    ): String {
        val docRef = db.collection(collectionPath).add(data).await()
        Log.d("FirestoreBase", "✅ Added to $collectionPath with ID: ${docRef.id}")
        return docRef.id
    }

    // 🟢 Lấy tất cả documents
    suspend fun getAll(collectionPath: String): List<DocumentSnapshot> {
        val result: QuerySnapshot = db.collection(collectionPath).get().await()
        return result.documents
    }

    // 🟢 Lấy document theo ID
    suspend fun getById(collectionPath: String, documentId: String): DocumentSnapshot? {
        val doc = db.collection(collectionPath).document(documentId).get().await()
        return if (doc.exists()) doc else null
    }

    // 🟢 Cập nhật document
    suspend fun updateData(
        collectionPath: String,
        documentId: String,
        updates: Map<String, Any?>
    ) {
        db.collection(collectionPath).document(documentId).update(updates).await()
        Log.d("FirestoreBase", "✅ Updated $collectionPath/$documentId")
    }

    // 🟢 Xóa document
    suspend fun deleteData(collectionPath: String, documentId: String) {
        db.collection(collectionPath).document(documentId).delete().await()
        Log.d("FirestoreBase", "🗑 Deleted $collectionPath/$documentId")
    }

    // 🟢 Lấy document theo field cụ thể (whereEqualTo)
    suspend fun getSingleBy(
        collectionPath: String,
        property: String,
        value: Any
    ): DocumentSnapshot? {
        val result = db.collection(collectionPath)
            .whereEqualTo(property, value)
            .limit(1)
            .get()
            .await()
        return result.documents.firstOrNull()
    }

    // 🟢 Lấy danh sách theo thuộc tính (whereEqualTo)
    suspend fun getListBy(
        collectionPath: String,
        property: String,
        value: Any
    ): List<DocumentSnapshot> {
        val result = db.collection(collectionPath)
            .whereEqualTo(property, value)
            .get()
            .await()
        return result.documents
    }

    /**
     * ⭐️ (MỚI) Lấy danh sách theo 'array-contains' (Dùng cho Product categories)
     */
    suspend fun getListByArrayContains(
        collectionPath: String,
        property: String,
        value: Any
    ): List<DocumentSnapshot> {
        val result = db.collection(collectionPath)
            .whereArrayContains(property, value)
            .get()
            .await()
        return result.documents
    }

    /**
     * Lấy dữ liệu từ Firestore theo nhiều điều kiện key -> value
     * @param collectionName Tên collection
     * @param conditions Map các điều kiện field -> value
     * @return List<DocumentSnapshot>
     */
    suspend fun getDataWhere(
        collectionName: String,
        conditions: Map<String, Any>
    ): List<DocumentSnapshot> = suspendCancellableCoroutine { cont ->

        var query: Query = db.collection(collectionName)

        // Thêm các điều kiện whereEqualTo
        for ((field, value) in conditions) {
            query = query.whereEqualTo(field, value)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                cont.resume(snapshot.documents) {}
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }
    /**
     * ⭐️ (THÊM HÀM MỚI NÀY)
     * Lấy dữ liệu với truy vấn khoảng (range query)
     * @param collectionPath Tên collection
     * @param rangeConditions Danh sách các điều kiện, ví dụ:
     * Pair("createdAt", ">=" to Timestamp(startDate))
     */
    suspend fun getDataWithRangeQuery(
        collectionPath: String,
        vararg rangeConditions: Pair<String, Pair<String, Any>>
    ): List<DocumentSnapshot> {
        return try {
            var query: Query = db.collection(collectionPath)

            // Thêm các điều kiện (>, <, >=, <=)
            for ((field, condition) in rangeConditions) {
                val operator = condition.first
                val value = condition.second
                query = when (operator) {
                    ">" -> query.whereGreaterThan(field, value)
                    "<" -> query.whereLessThan(field, value)
                    ">=" -> query.whereGreaterThanOrEqualTo(field, value)
                    "<=" -> query.whereLessThanOrEqualTo(field, value)
                    "==" -> query.whereEqualTo(field, value)
                    else -> query // Bỏ qua nếu toán tử không hợp lệ
                }
            }

            query.get().await().documents
        } catch (e: Exception) {
            Log.e("FirestoreBase", "Lỗi getDataWithRangeQuery: ${e.message}", e)
            emptyList()
        }
    }
}
