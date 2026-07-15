package org.ReDiego0.combatSystem.gui

import org.ReDiego0.combatSystem.loadout.LoadoutManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.plugin.java.JavaPlugin

class LoadoutListener(
    private val plugin: JavaPlugin,
    private val loadoutManager: LoadoutManager,
    private val loadoutGUI: LoadoutGUI
) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[LoadoutListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val title = event.view.title

        if (title != LoadoutGUI.TITLE) return

        event.isCancelled = true

        val slot = event.rawSlot
        if (slot < 0 || slot >= LoadoutGUI.INVENTORY_SIZE) return

        when (slot) {
            LoadoutGUI.CLOSE_SLOT -> {
                player.closeInventory()
            }
            LoadoutGUI.RESOURCE_STORAGE_SLOT -> {
                player.closeInventory()
                player.sendMessage("§e[Loadout] Resource storage opened (command: /recursos)")
            }
            in LoadoutGUI.WEAPON_SLOTS -> {
                val weaponIndex = LoadoutGUI.WEAPON_SLOTS.indexOf(slot)
                handleWeaponSlot(player, weaponIndex)
            }
            in LoadoutGUI.ARMOR_SLOTS -> {
                val armorIndex = LoadoutGUI.ARMOR_SLOTS.indexOf(slot)
                handleArmorSlot(player, armorIndex)
            }
            in LoadoutGUI.SUPPORT_SLOTS -> {
                val supportIndex = LoadoutGUI.SUPPORT_SLOTS.indexOf(slot)
                handleSupportSlot(player, supportIndex)
            }
        }
    }

    private fun handleWeaponSlot(player: Player, slot: Int) {
        val heldItem = player.inventory.itemInMainHand
        if (heldItem.type.isEmpty) {
            loadoutManager.updateWeapon(player.uniqueId, slot, null)
            player.sendMessage("§a[Loadout] Weapon ${slot + 1} cleared")
        } else {
            loadoutManager.updateWeapon(player.uniqueId, slot, heldItem.clone())
            player.sendMessage("§a[Loadout] Weapon ${slot + 1} set to ${heldItem.itemMeta?.displayName ?: heldItem.type.name}")
        }
        loadoutGUI.open(player)
    }

    private fun handleArmorSlot(player: Player, slot: Int) {
        val heldItem = player.inventory.itemInMainHand
        if (heldItem.type.isEmpty) {
            loadoutManager.updateArmor(player.uniqueId, slot, null)
            player.sendMessage("§a[Loadout] Armor slot ${slot + 1} cleared")
        } else {
            loadoutManager.updateArmor(player.uniqueId, slot, heldItem.clone())
            player.sendMessage("§a[Loadout] Armor slot ${slot + 1} set")
        }
        loadoutGUI.open(player)
    }

    private fun handleSupportSlot(player: Player, slot: Int) {
        val heldItem = player.inventory.itemInMainHand
        if (heldItem.type.isEmpty) {
            loadoutManager.updateSupportItem(player.uniqueId, slot, null)
            player.sendMessage("§a[Loadout] Support ${slot + 1} cleared")
        } else {
            loadoutManager.updateSupportItem(player.uniqueId, slot, heldItem.clone())
            player.sendMessage("§a[Loadout] Support ${slot + 1} set")
        }
        loadoutGUI.open(player)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val title = event.view.title

        if (title == LoadoutGUI.TITLE) {
            loadoutManager.savePlayerLoadout(player.uniqueId)
        }
    }
}
