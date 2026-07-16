package org.ReDiego0.combatSystem.listener

import org.ReDiego0.combatSystem.loot.LootEngine
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin

class LootMobKillListener(
    private val plugin: JavaPlugin,
    private val lootEngine: LootEngine
) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[LootMobKillListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        if (killer !is Player) return

        val entity = event.entity
        lootEngine.evaluateMobKill(killer, entity.type, entity.location)

        if (plugin.config.getBoolean("debug", false)) {
            plugin.logger.info("[LootMobKillListener] ${killer.name} killed ${entity.type} at ${entity.location}")
        }
    }
}
