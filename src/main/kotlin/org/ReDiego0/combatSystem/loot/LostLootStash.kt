package org.ReDiego0.combatSystem.loot

import org.ReDiego0.combatSystem.database.DatabaseManager
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.UUID

class LostLootStash(
    private val plugin: JavaPlugin,
    private val databaseManager: DatabaseManager
) {

    companion object {
        const val MAX_ITEMS = 50
    }

    fun init() {
        databaseManager.getConnection().prepareStatement("""
            CREATE TABLE IF NOT EXISTS lost_loot (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid TEXT NOT NULL,
                item_data TEXT NOT NULL,
                timestamp BIGINT NOT NULL
            )
        """).use { it.execute() }
    }

    fun addItem(uuid: UUID, item: ItemStack) {
        val serialized = serializeItem(item) ?: return
        val timestamp = System.currentTimeMillis()

        databaseManager.executeUpdate(
            "INSERT INTO lost_loot (uuid, item_data, timestamp) VALUES (?, ?, ?)",
            uuid.toString(), serialized, timestamp
        )
    }

    fun getItems(uuid: UUID): List<Pair<Int, ItemStack>> {
        val items = mutableListOf<Pair<Int, ItemStack>>()

        databaseManager.executeQuery(
            "SELECT id, item_data FROM lost_loot WHERE uuid = ? ORDER BY timestamp DESC LIMIT ?",
            uuid.toString(), MAX_ITEMS
        ) { rs ->
            while (rs.next()) {
                val id = rs.getInt("id")
                val item = deserializeItem(rs.getString("item_data"))
                if (item != null) {
                    items.add(Pair(id, item))
                }
            }
        }

        return items
    }

    fun removeItem(id: Int) {
        databaseManager.executeUpdate("DELETE FROM lost_loot WHERE id = ?", id)
    }

    fun clearExpired(expiryHours: Int) {
        val cutoff = System.currentTimeMillis() - (expiryHours * 3600 * 1000L)
        databaseManager.executeUpdate("DELETE FROM lost_loot WHERE timestamp < ?", cutoff)
    }

    fun getItemCount(uuid: UUID): Int {
        var count = 0
        databaseManager.executeQuery(
            "SELECT COUNT(*) as cnt FROM lost_loot WHERE uuid = ?",
            uuid.toString()
        ) { rs ->
            if (rs.next()) count = rs.getInt("cnt")
        }
        return count
    }

    private fun serializeItem(item: ItemStack): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)
            dataOutput.writeObject(item)
            dataOutput.close()
            Base64.getEncoder().encodeToString(outputStream.toByteArray())
        } catch (e: Exception) {
            plugin.logger.warning("[LostLootStash] Failed to serialize item: ${e.message}")
            null
        }
    }

    private fun deserializeItem(data: String): ItemStack? {
        return try {
            val bytes = Base64.getDecoder().decode(data)
            val inputStream = ByteArrayInputStream(bytes)
            val dataInput = BukkitObjectInputStream(inputStream)
            val item = dataInput.readObject() as ItemStack
            dataInput.close()
            item
        } catch (e: Exception) {
            plugin.logger.warning("[LostLootStash] Failed to deserialize item: ${e.message}")
            null
        }
    }
}
