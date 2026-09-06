package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.tasker.chronos.data.models.ShoppingItem
import com.tasker.chronos.data.models.ShoppingCategory
import com.tasker.chronos.data.repository.ShoppingRepository
import com.tasker.chronos.data.repository.ShoppingData

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShoppingRepository(application)

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items.asStateFlow()

    private val _categories = MutableStateFlow<List<ShoppingCategory>>(emptyList())
    val categories: StateFlow<List<ShoppingCategory>> = _categories.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val data = repository.loadData()
            _items.value = data.items
            _categories.value = data.categories
            android.util.Log.d("ShoppingViewModel", "Loaded ${data.items.size} items")
        }
    }

    private fun saveData() {
        viewModelScope.launch {
            repository.saveData(ShoppingData(
                items = _items.value,
                categories = _categories.value
            ))
        }
    }

    fun addItem(item: ShoppingItem) {
        _items.value = _items.value + item
        saveData()
        android.util.Log.d("ShoppingViewModel", "Added item: ${item.name}")
    }

    fun updateItem(item: ShoppingItem) {
        _items.value = _items.value.map {
            if (it.id == item.id) item
            else it
        }
        saveData()
    }

    fun deleteItem(itemId: String) {
        _items.value = _items.value.filter { it.id != itemId }
        saveData()
    }

    fun togglePurchased(itemId: String) {
        _items.value = _items.value.map {
            if (it.id == itemId) {
                it.copy(
                    isPurchased = !it.isPurchased,
                    purchasedAt = if (!it.isPurchased) java.time.LocalDateTime.now() else null
                )
            } else it
        }
        saveData()
    }

    fun addCategory(category: ShoppingCategory) {
        _categories.value = _categories.value + category
        saveData()
    }

    fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value.filter { it.id != categoryId }
        _items.value = _items.value.map { item ->
            val ids = item.resolvedCategoryIds().filter { it != categoryId }
            item.copy(categoryId = ids.firstOrNull(), categoryIds = ids)
        }
        saveData()
    }

    fun getTotalPrice(): Double {
        return _items.value.filter { !it.isPurchased }.sumOf { it.price }
    }

    fun getPurchasedTotal(): Double {
        return _items.value.filter { it.isPurchased }.sumOf { it.price }
    }

    fun remainingBudget(availableMoney: Double): Double {
        return availableMoney - getTotalPrice()
    }
}