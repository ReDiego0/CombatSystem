package org.ReDiego0.combatSystem.gui

import org.ReDiego0.combatSystem.loadout.LoadoutManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class SupportBagGUI(
    private val loadoutManager: LoadoutManager
) {

    companion object {
        const val INVENTORY_SIZE = 9
        const val TITLE = "§c§lTACTICAL BAG"
        val SUPPORT_SLOTS = intArrayOf(0, 1, 2, 3, 4, 5)
        const val CLOSE_SLOT = 8
    }

    fun open(player: Player) {
        val uuid = player.uniqueId
        val loadout = loadoutManager.getLoadout(uuid) ?: return

        val inventory = Bukkit.createInventory(null, INVENTORY_SIZE, TITLE)

        for (i in 0 until 6) {
            val item = loadout.supportItems.getOrNull(i)
            if (item != null) {
                inventory.setItem(SUPPORT_SLOTS[i], item.clone())
            } else {
                val emptyItem = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
                val meta = emptyItem.itemMeta
                meta?.setDisplayName("§7Empty Support Slot ${i + 1}")
                emptyItem.itemMeta = meta
                inventory.setItem(SUPPORT_SLOTS[i], emptyItem)
            }
        }

        val closeItem = ItemStack(Material.BARRIER)
        val closeMeta = closeItem.itemMeta
        closeMeta?.setDisplayName("§cClose")
        closeItem.itemMeta = closeMeta
        inventory.setItem(CLOSE_SLOT, closeItem)

        player.openInventory(inventory)
    }
}
