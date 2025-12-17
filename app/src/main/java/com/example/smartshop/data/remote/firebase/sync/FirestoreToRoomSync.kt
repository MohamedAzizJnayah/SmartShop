package com.example.smartshop.data.remote.firebase.sync

import com.example.smartshop.data.local.dao.ProductDao
import com.example.smartshop.data.mapper.toRoom
import com.example.smartshop.data.remote.firebase.dao.ProductFirestoreDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FirestoreToRoomSync(
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
                    is RemoteProductChange.Upsert -> local.upsert(change.p.toRoom())
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
