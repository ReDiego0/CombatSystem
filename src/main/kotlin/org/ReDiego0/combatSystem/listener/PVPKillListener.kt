package org.ReDiego0.combatSystem.listener

import org.ReDiego0.combatSystem.pvp.PVPManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class PVPKillListener(
    private val plugin: JavaPlugin,
    private val pvpManager: PVPManager
) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[PVPKillListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity
        val killer = victim.killer ?: return

        if (killer !is Player) return
        if (killer == victim) return

        pvpManager.onPlayerKill(killer, victim)

        if (plugin.config.getBoolean("debug", false)) {
            plugin.logger.info("[PVPKillListener] ${killer.name} killed ${victim.name}")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        pvpManager.clearPlayer(event.player)
    }
}
