package org.ReDiego0.combatSystem.loadout

import org.ReDiego0.combatSystem.item.ItemFactory
import org.ReDiego0.combatSystem.world.TownyIntegration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class LoadoutEquipper(
    private val plugin: JavaPlugin,
    private val loadoutManager: LoadoutManager,
    private val townyIntegration: TownyIntegration
) {

    fun equipLoadout(player: Player) {
        val uuid = player.uniqueId
        val loadout = loadoutManager.getLoadout(uuid) ?: return

        val vanillaItemsToStore = mutableListOf<ItemStack>()

        for (i in 0..8) {
            val item = player.inventory.getItem(i)
            if (item != null && !ItemFactory.isManagedWeapon(item)) {
                vanillaItemsToStore.add(item)
            }
        }

        for (i in 0..8) {
            player.inventory.setItem(i, null)
        }

        val overflowFromInventory = mutableListOf<ItemStack>()
        for (item in vanillaItemsToStore) {
            val emptySlot = player.inventory.firstEmpty()
            if (emptySlot != -1) {
                player.inventory.setItem(emptySlot, item)
            } else {
                overflowFromInventory.add(item)
            }
        }

        if (overflowFromInventory.isNotEmpty()) {
            val remaining = loadoutManager.addResourceItems(uuid, overflowFromInventory)
            if (remaining.isNotEmpty()) {
                for (item in remaining) {
                    player.world.dropItemNaturally(player.location, item)
                }
                player.sendMessage("§e[Loadout] Some items were dropped on the ground (storage full)")
            }
        }

        player.inventory.setItem(0, loadout.weapon1)
        player.inventory.setItem(1, loadout.weapon2)

        for (i in 0 until 6) {
            player.inventory.setItem(2 + i, loadout.supportItems.getOrNull(i))
        }

        player.inventory.helmet = loadout.helmet
        player.inventory.chestplate = loadout.chestplate
        player.inventory.leggings = loadout.leggings
        player.inventory.boots = loadout.boots

        loadoutManager.setLoadout(uuid, loadout.copy(isEquipped = true))

        player.sendMessage("§a[Loadout] Equipment equipped")
    }

    fun saveLoadout(player: Player) {
        val uuid = player.uniqueId
        val loadout = loadoutManager.getLoadout(uuid) ?: LoadoutData()

        val weapon1 = player.inventory.getItem(0)
        val weapon2 = player.inventory.getItem(1)

        val supportItems = mutableListOf<ItemStack?>()
        for (i in 2..7) {
            supportItems.add(player.inventory.getItem(i))
        }

        val helmet = player.inventory.helmet
        val chestplate = player.inventory.chestplate
        val leggings = player.inventory.leggings
        val boots = player.inventory.boots

        val updatedLoadout = loadout.copy(
            weapon1 = weapon1,
            weapon2 = weapon2,
            helmet = helmet,
            chestplate = chestplate,
            leggings = leggings,
            boots = boots,
            supportItems = supportItems,
            isEquipped = false
        )

        loadoutManager.setLoadout(uuid, updatedLoadout)
        loadoutManager.savePlayerLoadout(uuid)

        player.inventory.helmet = null
        player.inventory.chestplate = null
        player.inventory.leggings = null
        player.inventory.boots = null

        for (i in 0..8) {
            player.inventory.setItem(i, null)
        }

        val resources = loadoutManager.getResourceStorage(uuid)
        for (i in 0 until minOf(resources.size, player.inventory.size)) {
            if (resources[i] != null) {
                player.inventory.setItem(i, resources[i])
            }
        }

        player.sendMessage("§a[Loadout] Equipment saved")
    }

    fun isLoadoutEquipped(player: Player): Boolean {
        val loadout = loadoutManager.getLoadout(player.uniqueId)
        return loadout?.isEquipped == true
    }
}
