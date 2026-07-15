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

class ResourceStorage(
    private val plugin: JavaPlugin,
    private val databaseManager: DatabaseManager
) {

    companion object {
        const val STORAGE_SIZE = 27
    }

    fun saveStorage(uuid: UUID, items: List<ItemStack?>) {
        val sql = "INSERT OR REPLACE INTO resource_storage (uuid, items) VALUES (?, ?)"
        databaseManager.executeUpdate(sql, uuid.toString(), serializeItems(items))
    }

    fun loadStorage(uuid: UUID): List<ItemStack?> {
        var items: List<ItemStack?> = List(STORAGE_SIZE) { null }

        databaseManager.executeQuery(
            "SELECT items FROM resource_storage WHERE uuid = ?",
            uuid.toString()
        ) { rs ->
            if (rs.next()) {
                items = deserializeItems(rs.getString("items"))
            }
        }

        return items
    }

    fun hasSpace(items: List<ItemStack?>): Boolean {
        return items.count { it == null } > 0
    }

    fun addItems(uuid: UUID, itemsToAdd: List<ItemStack>): List<ItemStack> {
        val currentStorage = loadStorage(uuid).toMutableList()
        val overflow = mutableListOf<ItemStack>()

        for (item in itemsToAdd) {
            val emptySlot = currentStorage.indexOfFirst { it == null }
            if (emptySlot != -1) {
                currentStorage[emptySlot] = item
            } else {
                overflow.add(item)
            }
        }

        saveStorage(uuid, currentStorage)
        return overflow
    }

    private fun serializeItems(items: List<ItemStack?>): String {
        return items.joinToString("|") { item ->
            if (item == null) {
                "null"
            } else {
                try {
                    val outputStream = ByteArrayOutputStream()
                    val dataOutput = BukkitObjectOutputStream(outputStream)
                    dataOutput.writeObject(item)
                    dataOutput.close()
                    Base64.getEncoder().encodeToString(outputStream.toByteArray())
                } catch (e: Exception) {
                    "null"
                }
            }
        }
    }

    private fun deserializeItems(data: String?): List<ItemStack?> {
        if (data == null) return List(STORAGE_SIZE) { null }
        return data.split("|").map { entry ->
            if (entry == "null") {
                null
            } else {
                try {
                    val bytes = Base64.getDecoder().decode(entry)
                    val inputStream = ByteArrayInputStream(bytes)
                    val dataInput = BukkitObjectInputStream(inputStream)
                    val item = dataInput.readObject() as ItemStack
                    dataInput.close()
                    item
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}
