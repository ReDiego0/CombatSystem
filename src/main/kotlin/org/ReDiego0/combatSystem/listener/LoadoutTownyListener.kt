package org.ReDiego0.combatSystem.listener

import org.ReDiego0.combatSystem.loadout.LoadoutEquipper
import org.ReDiego0.combatSystem.loadout.LoadoutManager
import org.ReDiego0.combatSystem.world.TownyIntegration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class LoadoutTownyListener(
    private val plugin: JavaPlugin,
    private val townyIntegration: TownyIntegration,
    private val loadoutEquipper: LoadoutEquipper,
    private val loadoutManager: LoadoutManager
) : Listener {

    private val playerInCity = ConcurrentHashMap<UUID, Boolean>()

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[LoadoutTownyListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player

        if (event.from.blockX == event.to.blockX &&
            event.from.blockY == event.to.blockY &&
            event.from.blockZ == event.to.blockZ) {
            return
        }

        if (!townyIntegration.isAvailable()) return

        val uuid = player.uniqueId
        val wasInCity = playerInCity.getOrDefault(uuid, false)
        val isInCity = townyIntegration.isPlayerInCity(player)

        if (wasInCity && !isInCity) {
            onPlayerLeaveCity(player)
        } else if (!wasInCity && isInCity) {
            onPlayerEnterCity(player)
        }

        playerInCity[uuid] = isInCity
    }

    private fun onPlayerLeaveCity(player: Player) {
        val uuid = player.uniqueId

        if (!loadoutManager.hasLoadout(uuid)) {
            loadoutManager.loadPlayerLoadout(uuid)
        }

        loadoutEquipper.equipLoadout(player)

        if (plugin.config.getBoolean("debug", false)) {
            plugin.logger.info("[LoadoutTownyListener] ${player.name} left city - loadout equipped")
        }
    }

    private fun onPlayerEnterCity(player: Player) {
        val uuid = player.uniqueId

        if (loadoutEquipper.isLoadoutEquipped(player)) {
            loadoutEquipper.saveLoadout(player)
        }

        if (plugin.config.getBoolean("debug", false)) {
            plugin.logger.info("[LoadoutTownyListener] ${player.name} entered city - loadout saved")
        }
    }

    fun isPlayerInCity(player: Player): Boolean {
        return playerInCity.getOrDefault(player.uniqueId, false)
    }

    fun removePlayer(player: Player) {
        playerInCity.remove(player.uniqueId)
    }
}
