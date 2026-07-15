package org.ReDiego0.combatSystem.listener

import org.ReDiego0.combatSystem.data.PassiveTrigger
import org.ReDiego0.combatSystem.item.ArmorPassiveManager
import org.ReDiego0.combatSystem.item.ArmorRegistry
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.plugin.java.JavaPlugin

class ArmorPassiveListener(
    private val plugin: JavaPlugin,
    private val armorRegistry: ArmorRegistry,
    private val armorPassiveManager: ArmorPassiveManager
) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[ArmorPassiveListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        armorPassiveManager.onTrigger(player, PassiveTrigger.HIT_DEALT)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return

        armorPassiveManager.onTrigger(player, PassiveTrigger.HIT_TAKEN)

        if (event.cause == EntityDamageEvent.DamageCause.FIRE ||
            event.cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
            event.cause == EntityDamageEvent.DamageCause.LAVA) {
            armorPassiveManager.onTrigger(player, PassiveTrigger.FIRE_DAMAGE_TAKEN)
        }

        armorPassiveManager.onTrigger(player, PassiveTrigger.LOW_HP)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        if (killer is Player) {
            armorPassiveManager.onTrigger(killer, PassiveTrigger.ENEMY_KILLED)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        armorRegistry.updateEquippedArmor(player)
        armorPassiveManager.cleanupExpiredEffects(player)
    }
}
