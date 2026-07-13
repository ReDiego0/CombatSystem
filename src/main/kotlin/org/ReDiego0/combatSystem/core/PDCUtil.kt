package org.ReDiego0.combatSystem.core

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

object PDCUtil {

    private const val NAMESPACE = "combatsystem"
    private lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    fun key(key: String): NamespacedKey {
        return NamespacedKey(NAMESPACE, key)
    }

    fun getString(item: ItemStack, key: String): String? {
        return item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.STRING)
    }

    fun setString(item: ItemStack, key: String, value: String): ItemStack {
        val newItem = item.clone()
        val meta = newItem.itemMeta ?: return newItem
        meta.persistentDataContainer.set(key(key), PersistentDataType.STRING, value)
        newItem.itemMeta = meta
        return newItem
    }

    fun getInt(item: ItemStack, key: String): Int? {
        return item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.INTEGER)
    }

    fun setInt(item: ItemStack, key: String, value: Int): ItemStack {
        val newItem = item.clone()
        val meta = newItem.itemMeta ?: return newItem
        meta.persistentDataContainer.set(key(key), PersistentDataType.INTEGER, value)
        newItem.itemMeta = meta
        return newItem
    }

    fun getDouble(item: ItemStack, key: String): Double? {
        return item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.DOUBLE)
    }

    fun setDouble(item: ItemStack, key: String, value: Double): ItemStack {
        val newItem = item.clone()
        val meta = newItem.itemMeta ?: return newItem
        meta.persistentDataContainer.set(key(key), PersistentDataType.DOUBLE, value)
        newItem.itemMeta = meta
        return newItem
    }

    fun getBoolean(item: ItemStack, key: String): Boolean {
        val value = item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.BYTE)
        return value == 1.toByte()
    }

    fun setBoolean(item: ItemStack, key: String, value: Boolean): ItemStack {
        val newItem = item.clone()
        val meta = newItem.itemMeta ?: return newItem
        meta.persistentDataContainer.set(key(key), PersistentDataType.BYTE, if (value) 1 else 0)
        newItem.itemMeta = meta
        return newItem
    }

    fun getLong(item: ItemStack, key: String): Long? {
        return item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.LONG)
    }

    fun setLong(item: ItemStack, key: String, value: Long): ItemStack {
        val newItem = item.clone()
        val meta = newItem.itemMeta ?: return newItem
        meta.persistentDataContainer.set(key(key), PersistentDataType.LONG, value)
        newItem.itemMeta = meta
        return newItem
    }

    fun getFloat(item: ItemStack, key: String): Float? {
        return item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.FLOAT)
    }

    fun setFloat(item: ItemStack, key: String, value: Float): ItemStack {
        val newItem = item.clone()
        val meta = newItem.itemMeta ?: return newItem
        meta.persistentDataContainer.set(key(key), PersistentDataType.FLOAT, value)
        newItem.itemMeta = meta
        return newItem
    }

    fun hasCustomData(item: ItemStack): Boolean {
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.has(key("item_id"))
    }

    fun clearCustomData(item: ItemStack): ItemStack {
        val newItem = item.clone()
        val meta = newItem.itemMeta ?: return newItem
        val container = meta.persistentDataContainer
        container.keys.filter { it.namespace == NAMESPACE }.forEach { container.remove(it) }
        newItem.itemMeta = meta
        return newItem
    }

    fun getAllCustomData(item: ItemStack): Map<String, String> {
        val meta = item.itemMeta ?: return emptyMap()
        val container = meta.persistentDataContainer
        val result = mutableMapOf<String, String>()
        container.keys
            .filter { it.namespace == NAMESPACE }
            .forEach { key ->
                val value = container.get(key, PersistentDataType.STRING)
                result[key.key] = value ?: "N/A"
            }
        return result
    }
}
