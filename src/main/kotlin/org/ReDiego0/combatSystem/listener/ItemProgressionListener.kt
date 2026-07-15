package org.ReDiego0.combatSystem.listener

import org.ReDiego0.combatSystem.config.ConfigManager
import org.ReDiego0.combatSystem.item.ItemFactory
import org.ReDiego0.combatSystem.item.ItemProgression
import org.ReDiego0.combatSystem.world.WorldIsolation
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin

class ItemProgressionListener(
    private val plugin: JavaPlugin,
    private val worldIsolation: WorldIsolation,
    private val configManager: ConfigManager,
    private val itemProgression: ItemProgression
) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[ItemProgressionListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        if (killer !is Player) return

        val worldName = killer.world.name
        if (!worldIsolation.isWorldEnabled(worldName)) return

        val mobLevel = 1
        val totalXP = itemProgression.calculateMobXP(mobLevel)

        val mainHand = killer.inventory.itemInMainHand
        if (ItemFactory.isManagedWeapon(mainHand)) {
            val (updatedWeapon, weaponXP) = itemProgression.distributeXP(mainHand, totalXP)
            killer.inventory.setItemInMainHand(updatedWeapon)

            if (configManager.isDebug()) {
                plugin.logger.info("[ItemProgressionListener] ${killer.name} weapon gained ${"%.1f".format(weaponXP)} XP")
            }
        }

        val armorSlots = listOf(
            org.bukkit.inventory.EquipmentSlot.HEAD,
            org.bukkit.inventory.EquipmentSlot.CHEST,
            org.bukkit.inventory.EquipmentSlot.LEGS,
            org.bukkit.inventory.EquipmentSlot.FEET
        )

        val armorSharePerPiece = 0.125
        for (slot in armorSlots) {
            val armorPiece = killer.inventory.getItem(slot)
            if (armorPiece != null && ItemFactory.isManagedWeapon(armorPiece)) {
                val armorXP = totalXP * armorSharePerPiece
                val updatedArmor = itemProgression.addXP(armorPiece, armorXP)
                killer.inventory.setItem(slot, updatedArmor)

                if (configManager.isDebug()) {
                    plugin.logger.info("[ItemProgressionListener] ${killer.name} armor ($slot) gained ${"%.1f".format(armorXP)} XP")
                }
            }
        }
    }
}
