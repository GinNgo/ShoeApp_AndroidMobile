package data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resumeWithException

class FirestoreBase(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // 🟢 Thêm document
    suspend fun addData(
        collectionName: String,
        data: Map<String, Any?>
    ): String {
        val docRef = db.collection(collectionName).add(data).await()
        Log.d("FirestoreBase", "✅ Added to $collectionName with ID: ${docRef.id}")
        return docRef.id
    }

    // 🟢 Lấy tất cả documents
    suspend fun getAll(collectionName: String): List<DocumentSnapshot> {
        val result: QuerySnapshot = db.collection(collectionName).get().await()
        return result.documents
    }

    // 🟢 Lấy document theo ID
    suspend fun getById(collectionName: String, documentId: String): DocumentSnapshot? {
        val doc = db.collection(collectionName).document(documentId).get().await()
        return if (doc.exists()) doc else null
    }

    // 🟢 Cập nhật document
    suspend fun updateData(
        collectionName: String,
        documentId: String,
        updates: Map<String, Any?>
    ) {
        db.collection(collectionName).document(documentId).update(updates).await()
        Log.d("FirestoreBase", "✅ Updated $collectionName/$documentId")
    }

    // 🟢 Xóa document
    suspend fun deleteData(collectionName: String, documentId: String) {
        db.collection(collectionName).document(documentId).delete().await()
        Log.d("FirestoreBase", "🗑 Deleted $collectionName/$documentId")
    }

    // 🟢 Lấy document theo field cụ thể
    suspend fun getSingleBy(
        collection: String,
        property: String,
        value: Any
    ): DocumentSnapshot? {
        val result = db.collection(collection)
            .whereEqualTo(property, value)
            .limit(1)
            .get()
            .await()
        return result.documents.firstOrNull()
    }

    // 🟢 Lấy danh sách theo thuộc tính
    suspend fun getListBy(
        collection: String,
        property: String,
        value: Any
    ): List<DocumentSnapshot> {
        val result = db.collection(collection)
            .whereEqualTo(property, value)
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
}
