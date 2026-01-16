package com.tasker.chronos.data.repository

import android.content.Context
import com.tasker.chronos.data.models.ShoppingItem
import com.tasker.chronos.data.models.ShoppingCategory
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ShoppingData(
    val items: List<ShoppingItem> = emptyList(),
    val categories: List<ShoppingCategory> = emptyList()
)

class ShoppingRepository(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val file = File(context.filesDir, "shopping.json")

    fun loadData(): ShoppingData {
        return try {
            if (file.exists()) {
                val jsonString = file.readText()
                json.decodeFromString<ShoppingData>(jsonString)
            } else {
                ShoppingData()
            }
        } catch (e: Exception) {
            android.util.Log.e("ShoppingRepository", "Error loading shopping data", e)
            ShoppingData()
        }
    }

    fun saveData(data: ShoppingData) {
        try {
            val jsonString = json.encodeToString(data)
            file.writeText(jsonString)
            android.util.Log.d("ShoppingRepository", "Saved ${data.items.size} items, ${data.categories.size} categories")
        } catch (e: Exception) {
            android.util.Log.e("ShoppingRepository", "Error saving shopping data", e)
        }
    }
}