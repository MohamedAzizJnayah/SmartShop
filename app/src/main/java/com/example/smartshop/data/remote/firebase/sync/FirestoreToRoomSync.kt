package com.example.smartshop.data.remote.firebase.sync

import com.example.smartshop.data.local.dao.ProductDao
import com.example.smartshop.data.remote.firebase.dao.ProductFirestoreDao
import com.example.smartshop.domain.repository.ProductRepository
import com.example.smartshop.domain.model.RemoteProductChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreToRoomSync @Inject constructor(
    private val remote: ProductFirestoreDao,
    private val local: ProductDao
) {
    private var job: Job? = null

    fun start(scope: CoroutineScope, clearLocalFirst: Boolean = false) {
        if (job != null) return

        job = scope.launch(Dispatchers.IO) {
            if (clearLocalFirst) local.clearAll()

            remote.observeChanges().collect { change ->
                when (change) {
                    is RemoteProductChange.Upsert -> local.upsert(change.p)
                    is RemoteProductChange.Delete -> local.deleteById(change.id)
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
