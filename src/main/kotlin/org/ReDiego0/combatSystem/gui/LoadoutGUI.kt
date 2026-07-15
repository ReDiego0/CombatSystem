package org.ReDiego0.combatSystem.gui

import org.ReDiego0.combatSystem.loadout.LoadoutData
import org.ReDiego0.combatSystem.loadout.LoadoutManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID

class LoadoutGUI(
    private val loadoutManager: LoadoutManager
) {

    companion object {
        const val INVENTORY_SIZE = 54
        const val TITLE = "§6§lLOADOUT"

        val WEAPON_SLOTS = intArrayOf(10, 11)
        val ARMOR_SLOTS = intArrayOf(19, 28, 37, 29)
        val SUPPORT_SLOTS = intArrayOf(21, 22, 23, 30, 31, 32)

        const val RESOURCE_STORAGE_SLOT = 49
        const val CLOSE_SLOT = 45
    }

    fun open(player: Player) {
        val uuid = player.uniqueId
        val loadout = loadoutManager.getLoadout(uuid) ?: LoadoutData()

        val inventory = Bukkit.createInventory(null, INVENTORY_SIZE, TITLE)

        fillBackground(inventory)

        inventory.setItem(WEAPON_SLOTS[0], createSlotItem(Material.DIAMOND_SWORD, "§6Weapon 1", loadout.weapon1))
        inventory.setItem(WEAPON_SLOTS[1], createSlotItem(Material.BOW, "§6Weapon 2", loadout.weapon2))

        inventory.setItem(ARMOR_SLOTS[0], createSlotItem(Material.DIAMOND_HELMET, "§6Helmet", loadout.helmet))
        inventory.setItem(ARMOR_SLOTS[1], createSlotItem(Material.DIAMOND_CHESTPLATE, "§6Chestplate", loadout.chestplate))
        inventory.setItem(ARMOR_SLOTS[2], createSlotItem(Material.DIAMOND_LEGGINGS, "§6Leggings", loadout.leggings))
        inventory.setItem(ARMOR_SLOTS[3], createSlotItem(Material.DIAMOND_BOOTS, "§6Boots", loadout.boots))

        for (i in 0 until 6) {
            val item = loadout.supportItems.getOrNull(i)
            inventory.setItem(SUPPORT_SLOTS[i], createSlotItem(Material.POTION, "§6Support ${i + 1}", item))
        }

        val resourceItem = ItemStack(Material.CHEST)
        val resourceMeta = resourceItem.itemMeta
        resourceMeta?.setDisplayName("§eResource Storage")
        resourceMeta?.lore = listOf("§7Click to open resource storage", "§7(${loadoutManager.getResourceStorage(uuid).count { it != null }}/27 slots used)")
        resourceItem.itemMeta = resourceMeta
        inventory.setItem(RESOURCE_STORAGE_SLOT, resourceItem)

        val closeItem = ItemStack(Material.BARRIER)
        val closeMeta = closeItem.itemMeta
        closeMeta?.setDisplayName("§cClose")
        closeItem.itemMeta = closeMeta
        inventory.setItem(CLOSE_SLOT, closeItem)

        player.openInventory(inventory)
    }

    private fun createSlotItem(material: Material, name: String, currentItem: ItemStack?): ItemStack {
        if (currentItem != null) {
            return currentItem.clone()
        }

        val item = ItemStack(material)
        val meta = item.itemMeta
        meta?.setDisplayName(name)
        meta?.lore = listOf("§7Empty - Click to set")
        item.itemMeta = meta
        return item
    }

    private fun fillBackground(inventory: Inventory) {
        val filler = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        val meta = filler.itemMeta
        meta?.setDisplayName(" ")
        filler.itemMeta = meta

        for (i in 0 until INVENTORY_SIZE) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler)
            }
        }
    }
}
