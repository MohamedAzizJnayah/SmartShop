package com.example.smartshop.ui.viewmodel.product


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repo: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductUiState())
    val state: StateFlow<ProductUiState> = _state.asStateFlow()

    init {
        // Observe Room (via repo.observeAll) + stats
        viewModelScope.launch {
            combine(
                repo.observeAll(),
                repo.observeStats()
            ) { products, stats ->
                products to stats
            }.onEach { (products, stats) ->
                _state.update {
                    it.copy(
                        products = products,
                        stats = stats,
                        isLoading = false,
                        error = null
                    )
                }
            }.catch { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }.collect()
        }
    }

    fun addProduct(name: String, quantity: Int, price: Double) {
        viewModelScope.launch {
            runCatching {
                val p = Product(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    quantity = quantity,
                    price = price,
                    updatedAt = System.currentTimeMillis()
                )
                repo.upsert(p)
            }.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Erreur") }
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            runCatching { repo.upsert(product.copy(updatedAt = System.currentTimeMillis())) }
                .onFailure { e -> _state.update { it.copy(error = e.message ?: "Erreur") } }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            runCatching { repo.delete(id) }
                .onFailure { e -> _state.update { it.copy(error = e.message ?: "Erreur") } }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
