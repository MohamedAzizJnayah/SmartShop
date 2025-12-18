package com.example.smartshop.domain.model

import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity

sealed interface RemoteProductChange {
    data class Upsert(val p: Product) : RemoteProductChange
    data class Delete(val id: String) : RemoteProductChange
}