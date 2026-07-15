package org.ReDiego0.combatSystem.loadout

import org.ReDiego0.combatSystem.database.DatabaseManager
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.bukkit.plugin.java.JavaPlugin
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.UUID

class LoadoutStorage(
    private val plugin: JavaPlugin,
    private val databaseManager: DatabaseManager
) {

    fun saveLoadout(uuid: UUID, loadout: LoadoutData) {
        val sql = """
            INSERT OR REPLACE INTO loadouts (uuid, weapon1, weapon2, helmet, chestplate, leggings, boots, support_items, is_equipped)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        databaseManager.executeUpdate(
            sql,
            uuid.toString(),
            serializeItem(loadout.weapon1),
            serializeItem(loadout.weapon2),
            serializeItem(loadout.helmet),
            serializeItem(loadout.chestplate),
            serializeItem(loadout.leggings),
            serializeItem(loadout.boots),
            serializeSupportItems(loadout.supportItems),
            if (loadout.isEquipped) 1 else 0
        )
    }

    fun loadLoadout(uuid: UUID): LoadoutData? {
        var loadout: LoadoutData? = null

        databaseManager.executeQuery(
            "SELECT * FROM loadouts WHERE uuid = ?",
            uuid.toString()
        ) { rs ->
            if (rs.next()) {
                loadout = LoadoutData(
                    weapon1 = deserializeItem(rs.getString("weapon1")),
                    weapon2 = deserializeItem(rs.getString("weapon2")),
                    helmet = deserializeItem(rs.getString("helmet")),
                    chestplate = deserializeItem(rs.getString("chestplate")),
                    leggings = deserializeItem(rs.getString("leggings")),
                    boots = deserializeItem(rs.getString("boots")),
                    supportItems = deserializeSupportItems(rs.getString("support_items")),
                    isEquipped = rs.getInt("is_equipped") == 1
                )
            }
        }

        return loadout
    }

    fun deleteLoadout(uuid: UUID) {
        databaseManager.executeUpdate("DELETE FROM loadouts WHERE uuid = ?", uuid.toString())
    }

    private fun serializeItem(item: ItemStack?): String? {
        if (item == null) return null
        return try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)
            dataOutput.writeObject(item)
            dataOutput.close()
            Base64.getEncoder().encodeToString(outputStream.toByteArray())
        } catch (e: Exception) {
            plugin.logger.warning("[LoadoutStorage] Failed to serialize item: ${e.message}")
            null
        }
    }

    private fun deserializeItem(data: String?): ItemStack? {
        if (data == null) return null
        return try {
            val bytes = Base64.getDecoder().decode(data)
            val inputStream = ByteArrayInputStream(bytes)
            val dataInput = BukkitObjectInputStream(inputStream)
            val item = dataInput.readObject() as ItemStack
            dataInput.close()
            item
        } catch (e: Exception) {
            plugin.logger.warning("[LoadoutStorage] Failed to deserialize item: ${e.message}")
            null
        }
    }

    private fun serializeSupportItems(items: List<ItemStack?>): String {
        return items.joinToString("|") { serializeItem(it) ?: "null" }
    }

    private fun deserializeSupportItems(data: String?): List<ItemStack?> {
        if (data == null) return listOf(null, null, null, null, null, null)
        return data.split("|").map { if (it == "null") null else deserializeItem(it) }
    }
}
