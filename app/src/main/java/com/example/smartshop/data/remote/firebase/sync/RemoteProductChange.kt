package com.example.smartshop.data.remote.firebase.sync

import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity

sealed interface RemoteProductChange {
    data class Upsert(val p: ProductFirestoreEntity) : RemoteProductChange
    data class Delete(val id: String) : RemoteProductChange
}