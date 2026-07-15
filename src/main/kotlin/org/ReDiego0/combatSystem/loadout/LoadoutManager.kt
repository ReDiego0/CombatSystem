package org.ReDiego0.combatSystem.loadout

import org.ReDiego0.combatSystem.item.ItemFactory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class LoadoutManager(
    private val loadoutStorage: LoadoutStorage,
    private val resourceStorage: ResourceStorage
) {

    private val loadedLoadouts = ConcurrentHashMap<UUID, LoadoutData>()
    private val playerResourceStorage = ConcurrentHashMap<UUID, List<ItemStack?>>()

    fun loadPlayerLoadout(uuid: UUID) {
        val loadout = loadoutStorage.loadLoadout(uuid) ?: LoadoutData()
        loadedLoadouts[uuid] = loadout

        val resources = resourceStorage.loadStorage(uuid)
        playerResourceStorage[uuid] = resources
    }

    fun savePlayerLoadout(uuid: UUID) {
        val loadout = loadedLoadouts[uuid] ?: return
        loadoutStorage.saveLoadout(uuid, loadout)

        val resources = playerResourceStorage[uuid] ?: return
        resourceStorage.saveStorage(uuid, resources)
    }

    fun getLoadout(uuid: UUID): LoadoutData? {
        return loadedLoadouts[uuid]
    }

    fun setLoadout(uuid: UUID, loadout: LoadoutData) {
        loadedLoadouts[uuid] = loadout
    }

    fun getResourceStorage(uuid: UUID): List<ItemStack?> {
        return playerResourceStorage[uuid] ?: List(ResourceStorage.STORAGE_SIZE) { null }
    }

    fun setResourceStorage(uuid: UUID, items: List<ItemStack?>) {
        playerResourceStorage[uuid] = items
    }

    fun updateWeapon(uuid: UUID, slot: Int, item: ItemStack?) {
        val current = loadedLoadouts[uuid] ?: LoadoutData()
        val updated = when (slot) {
            0 -> current.copy(weapon1 = item)
            1 -> current.copy(weapon2 = item)
            else -> current
        }
        loadedLoadouts[uuid] = updated
    }

    fun updateArmor(uuid: UUID, slot: Int, item: ItemStack?) {
        val current = loadedLoadouts[uuid] ?: LoadoutData()
        val updated = when (slot) {
            0 -> current.copy(helmet = item)
            1 -> current.copy(chestplate = item)
            2 -> current.copy(leggings = item)
            3 -> current.copy(boots = item)
            else -> current
        }
        loadedLoadouts[uuid] = updated
    }

    fun updateSupportItem(uuid: UUID, slot: Int, item: ItemStack?) {
        val current = loadedLoadouts[uuid] ?: LoadoutData()
        val supportItems = current.supportItems.toMutableList()
        if (slot in supportItems.indices) {
            supportItems[slot] = item
            loadedLoadouts[uuid] = current.copy(supportItems = supportItems)
        }
    }

    fun removePlayer(uuid: UUID) {
        loadedLoadouts.remove(uuid)
        playerResourceStorage.remove(uuid)
    }

    fun hasLoadout(uuid: UUID): Boolean {
        return loadedLoadouts.containsKey(uuid)
    }

    fun addResourceItems(uuid: UUID, itemsToAdd: List<ItemStack>): List<ItemStack> {
        val currentStorage: MutableList<ItemStack?> = playerResourceStorage[uuid]?.toMutableList()
            ?: MutableList(ResourceStorage.STORAGE_SIZE) { null }
        val overflow = mutableListOf<ItemStack>()

        for (item in itemsToAdd) {
            val emptySlot = currentStorage.indexOfFirst { it == null }
            if (emptySlot != -1) {
                currentStorage[emptySlot] = item
            } else {
                overflow.add(item)
            }
        }

        playerResourceStorage[uuid] = currentStorage
        return overflow
    }
}
