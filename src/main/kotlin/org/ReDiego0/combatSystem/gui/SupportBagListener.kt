package org.ReDiego0.combatSystem.gui

import org.ReDiego0.combatSystem.loadout.LoadoutManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.java.JavaPlugin

class SupportBagListener(
    private val plugin: JavaPlugin,
    private val loadoutManager: LoadoutManager,
    private val supportBagGUI: SupportBagGUI
) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[SupportBagListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val title = event.view.title

        if (title != SupportBagGUI.TITLE) return

        event.isCancelled = true

        val slot = event.rawSlot
        if (slot < 0 || slot >= SupportBagGUI.INVENTORY_SIZE) return

        when (slot) {
            SupportBagGUI.CLOSE_SLOT -> {
                player.closeInventory()
            }
            in SupportBagGUI.SUPPORT_SLOTS -> {
                val supportIndex = SupportBagGUI.SUPPORT_SLOTS.indexOf(slot)
                handleSupportClick(player, supportIndex)
            }
        }
    }

    private fun handleSupportClick(player: Player, slot: Int) {
        val uuid = player.uniqueId
        val loadout = loadoutManager.getLoadout(uuid) ?: return
        val supportItem = loadout.supportItems.getOrNull(slot)

        if (supportItem != null) {
            player.inventory.setItem(player.inventory.heldItemSlot, supportItem.clone())
            player.sendMessage("§a[Support] Equipped support item ${slot + 1}")
        } else {
            player.sendMessage("§c[Support] No support item in slot ${slot + 1}")
        }

        player.closeInventory()
    }
}
