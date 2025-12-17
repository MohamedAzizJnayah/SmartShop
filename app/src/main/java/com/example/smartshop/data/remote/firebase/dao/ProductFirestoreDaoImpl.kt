package com.example.smartshop.data.remote.firebase.dao

import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity
import com.example.smartshop.data.remote.firebase.sync.RemoteProductChange
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductFirestoreDaoImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProductFirestoreDao {

    private fun col() =
        firestore.collection("users")
            .document(requireNotNull(auth.currentUser).uid)
            .collection("products")

    override fun observeAll(): Flow<List<ProductFirestoreEntity>> = callbackFlow {
        val reg = col().addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener

            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(ProductFirestoreEntity::class.java)?.copy(id = doc.id)
            }
            trySend(list)
        }

        awaitClose { reg.remove() }
    }

    override suspend fun upsert(product: ProductFirestoreEntity) {
        col().document(product.id).set(product).await()
    }

    override suspend fun delete(id: String) {
        col().document(id).delete().await()
    }

    override fun observeChanges(): Flow<RemoteProductChange> = callbackFlow {
        val reg = col().addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener

            for (change in snap.documentChanges) {
                when (change.type) {
                    DocumentChange.Type.ADDED,
                    DocumentChange.Type.MODIFIED -> {
                        val entity = change.document.toObject(ProductFirestoreEntity::class.java)
                            .copy(id = change.document.id)
                        trySend(RemoteProductChange.Upsert(entity))
                    }
                    DocumentChange.Type.REMOVED -> {
                        trySend(RemoteProductChange.Delete(change.document.id))
                    }
                }
            }
        }
        awaitClose { reg.remove() }
    }
}
