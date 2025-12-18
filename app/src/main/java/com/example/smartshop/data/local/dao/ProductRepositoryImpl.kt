package com.example.smartshop.data.local.dao

import com.example.smartshop.data.mapper.toFirestoreEntity
import com.example.smartshop.data.remote.firebase.dao.ProductFirestoreDao
import com.example.smartshop.domain.model.Product
import com.example.smartshop.domain.model.StockStats
import com.example.smartshop.domain.repository.ProductRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,                // Room
    private val remote: ProductFirestoreDao            // Firestore
) : ProductRepository {

    override fun observeAll() = productDao.observeAll().map { list -> list.map { it } }

    override suspend fun upsert(product: Product) {
        // 1) Room
        productDao.upsert(product)
        // 2) Firestore
        remote.upsert(product.toFirestoreEntity())
    }

    override suspend fun delete(id: String) {
        productDao.deleteById(id)
        remote.delete(id)
    }

    override fun observeChanges() = remote.observeChanges()

    override fun observeStats() = combine(productDao.observeCount(), productDao.observeTotalValue()) { c, v ->
        StockStats(c, v)
    }
}
