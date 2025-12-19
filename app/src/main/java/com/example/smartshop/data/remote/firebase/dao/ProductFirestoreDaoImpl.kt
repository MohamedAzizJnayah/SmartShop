package com.example.smartshop.data.remote.firebase.dao

import com.example.smartshop.data.mapper.toRoom
import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity
import com.example.smartshop.domain.model.RemoteProductChange
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductFirestoreDaoImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProductFirestoreDao {

    /**
     * Retourne la collection Firestore de l'utilisateur connecté
     * Lance une exception claire si l'utilisateur n'est pas authentifié
     */
    private fun col() = run {
        val user = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

        firestore.collection("users")
            .document(user.uid)
            .collection("products")
    }

    /**
     * Observe toute la liste des produits
     */
    override fun observeAll(): Flow<List<com.example.smartshop.domain.model.Product>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            close()
            return@callbackFlow
        }

        val reg = firestore.collection("users")
            .document(user.uid)
            .collection("products")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener

                launch(Dispatchers.Default) {
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

    /**
     * Insert ou update un produit
     */
    override suspend fun upsert(entity: ProductFirestoreEntity) {
        col()
            .document(entity.id)
            .set(entity)
            .await()
    }

    /**
     * Supprimer un produit
     */
    override suspend fun delete(id: String) {
        col()
            .document(id)
            .delete()
            .await()
    }

    /**
     * Observe les changements Firestore (ADD / UPDATE / DELETE)
     */
    override fun observeChanges(): Flow<RemoteProductChange> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            close()
            return@callbackFlow
        }

        val reg = firestore.collection("users")
            .document(user.uid)
            .collection("products")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener

                for (change in snap.documentChanges) {
                    when (change.type) {

                        DocumentChange.Type.ADDED,
                        DocumentChange.Type.MODIFIED -> {
                            val entity = change.document
                                .toObject(ProductFirestoreEntity::class.java)
                                .copy(id = change.document.id)
                                .toRoom()

                            trySend(RemoteProductChange.Upsert(entity))
                        }

                        DocumentChange.Type.REMOVED -> {
                            trySend(
                                RemoteProductChange.Delete(
                                    change.document.id
                                )
                            )
                        }
                    }
                }
            }

        awaitClose { reg.remove() }
    }
}
