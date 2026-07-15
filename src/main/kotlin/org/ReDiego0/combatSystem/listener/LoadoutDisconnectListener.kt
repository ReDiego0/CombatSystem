package org.ReDiego0.combatSystem.listener

import org.ReDiego0.combatSystem.loadout.LoadoutEquipper
import org.ReDiego0.combatSystem.loadout.LoadoutManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class LoadoutDisconnectListener(
    private val plugin: JavaPlugin,
    private val loadoutEquipper: LoadoutEquipper,
    private val loadoutManager: LoadoutManager
) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[LoadoutDisconnectListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId

        if (loadoutEquipper.isLoadoutEquipped(player)) {
            loadoutEquipper.saveLoadout(player)

            if (plugin.config.getBoolean("debug", false)) {
                plugin.logger.info("[LoadoutDisconnectListener] ${player.name} disconnected - loadout saved")
            }
        }

        loadoutManager.removePlayer(uuid)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId

        loadoutManager.loadPlayerLoadout(uuid)

        val loadout = loadoutManager.getLoadout(uuid)
        if (loadout != null && loadout.isEquipped) {
            loadoutEquipper.equipLoadout(player)

            if (plugin.config.getBoolean("debug", false)) {
                plugin.logger.info("[LoadoutDisconnectListener] ${player.name} joined - loadout equipped")
            }
        }
    }
}
