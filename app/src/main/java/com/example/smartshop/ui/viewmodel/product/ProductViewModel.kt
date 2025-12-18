package com.example.smartshop.ui.viewmodel.product


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.data.remote.firebase.sync.FirestoreToRoomSync
import com.example.smartshop.domain.model.Product
import com.example.smartshop.domain.model.StockStats
import com.example.smartshop.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


data class ProductUiState(
    val products: List<Product> = emptyList(),
    val stats: StockStats = StockStats(totalProducts = 0, totalStockValue = 0.0),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repo: ProductRepository,
    private val sync: FirestoreToRoomSync
) : ViewModel() {

    private val _state = MutableStateFlow(ProductUiState())
    val state: StateFlow<ProductUiState> = _state.asStateFlow()

    init {
        sync.start(viewModelScope, clearLocalFirst = false)

        viewModelScope.launch {
            combine(repo.observeAll(), repo.observeStats()) { products, stats ->
                products to stats
            }
                .onEach { (products, stats) ->
                    _state.update {
                        it.copy(
                            products = products,
                            stats = stats,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Erreur") }
                }
                .collect()
        }
    }

    fun addProduct(name: String, quantity: Int, price: Double) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) {
            _state.update { it.copy(error = "Nom obligatoire") }
            return
        }

        viewModelScope.launch {
            runCatching {
                val p = Product(
                    id = UUID.randomUUID().toString(),
                    name = cleanName,
                    quantity = quantity,
                    price = price,
                    updatedAt = System.currentTimeMillis()
                )
                repo.upsert(p) // ✅ écrit Room + Firestore
            }.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Erreur ajout") }
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            runCatching {
                repo.upsert(product.copy(updatedAt = System.currentTimeMillis()))
            }.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Erreur modification") }
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            runCatching {
                repo.delete(id) // ✅ supprime Room + Firestore
            }.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Erreur suppression") }
            }
        }
    }

    fun syncNow(clearLocalFirst: Boolean = false) {
        sync.stop()
        sync.start(viewModelScope, clearLocalFirst)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun importProducts(items: List<Product>) {
        if (items.isEmpty()) {
            _state.update { it.copy(error = "Aucun produit trouvé dans le PDF") }
            return
        }

        viewModelScope.launch {
            runCatching {
                items.forEach { repo.upsert(it.copy(updatedAt = System.currentTimeMillis())) }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Erreur import PDF") }
            }
        }
    }

}



