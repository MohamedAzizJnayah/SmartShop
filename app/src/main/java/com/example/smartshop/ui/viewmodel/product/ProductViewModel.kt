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
                        it.copy(products = products, stats = stats, isLoading = false, error = null)
                    }
                }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Erreur") }
                }
                .collect()
        }
    }

    fun syncNow(clearLocalFirst: Boolean = false) {
        sync.stop()
        sync.start(viewModelScope, clearLocalFirst = clearLocalFirst)
    }

    fun addProduct(name: String, quantity: Int, price: Double) {
        val clean = name.trim()
        if (clean.isBlank()) {
            _state.update { it.copy(error = "Le nom est obligatoire") }
            return
        }

        viewModelScope.launch {
            runCatching {
                val p = Product(
                    id = UUID.randomUUID().toString(),
                    name = clean,
                    quantity = quantity,
                    price = price,
                    updatedAt = System.currentTimeMillis()
                )
                repo.upsert(p)
            }.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Erreur ajout") }
            }
        }
    }

    fun updateProduct(product: Product) {
        val clean = (product.name ?: "").trim()
        if (clean.isBlank()) {
            _state.update { it.copy(error = "Le nom est obligatoire") }
            return
        }

        viewModelScope.launch {
            runCatching {
                repo.upsert(product.copy(name = clean, updatedAt = System.currentTimeMillis()))
            }.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Erreur modification") }
            }
        }
    }

    fun deleteProduct(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            runCatching { repo.delete(id) }
                .onFailure { e -> _state.update { it.copy(error = e.message ?: "Erreur suppression") } }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        // sync.stop() // optionnel
    }
}
