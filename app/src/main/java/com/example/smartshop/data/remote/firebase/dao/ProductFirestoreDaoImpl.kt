package com.example.smartshop.data.remote.firebase.dao

import com.example.smartshop.data.mapper.toRoom
import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity
import com.example.smartshop.domain.model.RemoteProductChange
import com.example.smartshop.domain.model.Product
import com.example.smartshop.domain.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Singleton

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@Singleton
class ProductFirestoreDaoImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProductFirestoreDao {

    private fun col() =
        firestore.collection("users")
            .document(requireNotNull(auth.currentUser).uid)
            .collection("products")

    override fun observeAll() = callbackFlow {
        val reg = col().addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener

            launch(kotlinx.coroutines.Dispatchers.Default) {
                val list = snap.documents.mapNotNull { doc ->
                    doc.toObject(ProductFirestoreEntity::class.java)
                        ?.copy(id = doc.id)
                        ?.toRoom()
                }
                trySend(list)
            }
        }
        awaitClose { reg.remove() }
    }


    override suspend fun upsert(entity: ProductFirestoreEntity) {
        col().document(entity.id).set(entity).await()
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
                            .copy(id = change.document.id).toRoom()
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
