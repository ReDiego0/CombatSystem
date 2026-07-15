package org.ReDiego0.combatSystem.gui

import org.ReDiego0.combatSystem.item.TraitEngine
import org.ReDiego0.combatSystem.item.TraitLoader
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.plugin.java.JavaPlugin

class ArmoryListener(
    private val plugin: JavaPlugin,
    private val traitEngine: TraitEngine,
    private val traitLoader: TraitLoader
) : Listener {

    private val openArmories = mutableMapOf<Player, org.bukkit.inventory.ItemStack>()

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[ArmoryListener] Registered successfully")
    }

    fun openArmory(player: Player, item: org.bukkit.inventory.ItemStack) {
        openArmories[player] = item
        val gui = ArmoryGUI(traitEngine, traitLoader)
        gui.open(player, item)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val title = event.view.title

        if (!title.startsWith(ArmoryGUI.TITLE_PREFIX)) return

        event.isCancelled = true

        val slot = event.rawSlot
        if (slot < 0 || slot >= ArmoryGUI.INVENTORY_SIZE) return

        val item = openArmories[player] ?: return

        val columnSlotPair = ArmoryGUI.COLUMN_SLOTS.indexOfFirst { it.first == slot || it.second == slot }
        if (columnSlotPair == -1) return

        val (slot1, slot2) = ArmoryGUI.COLUMN_SLOTS[columnSlotPair]
        val rowIndex = if (slot == slot1) 0 else 1

        val columns = traitEngine.getTraitColumns(item)
        if (columnSlotPair >= columns.size) return
        if (rowIndex >= columns[columnSlotPair].size) return

        val traitId = columns[columnSlotPair][rowIndex]
        val isUnlocked = traitEngine.isTraitUnlocked(item, columnSlotPair, rowIndex)

        if (!isUnlocked) {
            player.sendMessage("§c[Trait] This trait is locked. Level up your weapon first.")
            return
        }

        val activeTraits = traitEngine.getActiveTraits(item)
        if (activeTraits[columnSlotPair.toString()] == traitId) {
            player.sendMessage("§e[Trait] This trait is already active.")
            return
        }

        val updatedItem = traitEngine.setActiveTrait(item, columnSlotPair, traitId)
        openArmories[player] = updatedItem

        player.inventory.setItemInMainHand(updatedItem)

        val trait = traitLoader.get(traitId)
        player.sendMessage("§a[Trait] Activated: ${trait?.displayName ?: traitId}")

        val gui = ArmoryGUI(traitEngine, traitLoader)
        gui.open(player, updatedItem)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val title = event.view.title

        if (title.startsWith(ArmoryGUI.TITLE_PREFIX)) {
            openArmories.remove(player)
        }
    }

    fun isPlayerInArmory(player: Player): Boolean {
        return openArmories.containsKey(player)
    }
}
