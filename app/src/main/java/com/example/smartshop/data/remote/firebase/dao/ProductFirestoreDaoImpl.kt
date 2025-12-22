package com.example.smartshop.data.remote.firebase.dao


import com.example.smartshop.data.mapper.toRoom
import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity
import com.example.smartshop.domain.model.Product
import com.example.smartshop.domain.model.RemoteProductChange
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Singleton
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Singleton
class ProductFirestoreDaoImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProductFirestoreDao {

    private fun col(uid: String) =
        firestore.collection("users").document(uid).collection("products")

    private fun uidFlow(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)

        trySend(auth.currentUser?.uid)

        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override fun observeAll(): Flow<List<Product>> =
        uidFlow().flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val reg = col(uid).addSnapshotListener { snap, err ->
                        if (err != null || snap == null) return@addSnapshotListener

                        launch(Dispatchers.Default) {
                            val list: List<Product> = snap.documents.mapNotNull { doc ->
                                doc.toObject(ProductFirestoreEntity::class.java)
                                    ?.copy(id = doc.id)
                                    ?.toRoom()
                            }
                            trySend(list)
                        }
                    }
                    awaitClose { reg.remove() }
                }
            }
        }

    override fun observeChanges(): Flow<RemoteProductChange> =
        uidFlow().flatMapLatest { uid ->
            if (uid == null) {
                emptyFlow()
            } else {
                callbackFlow {
                    val reg = col(uid).addSnapshotListener { snap, err ->
                        if (err != null || snap == null) return@addSnapshotListener

                        for (change in snap.documentChanges) {
                            when (change.type) {
                                DocumentChange.Type.ADDED,
                                DocumentChange.Type.MODIFIED -> {
                                    val product = change.document
                                        .toObject(ProductFirestoreEntity::class.java)
                                        .copy(id = change.document.id)
                                        .toRoom()
                                    trySend(RemoteProductChange.Upsert(product))
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
        }

    override suspend fun upsert(entity: ProductFirestoreEntity) {
        val uid = auth.currentUser?.uid ?: return
        col(uid).document(entity.id).set(entity).await()
    }

    override suspend fun delete(id: String) {
        val uid = auth.currentUser?.uid ?: return
        col(uid).document(id).delete().await()
    }
}
