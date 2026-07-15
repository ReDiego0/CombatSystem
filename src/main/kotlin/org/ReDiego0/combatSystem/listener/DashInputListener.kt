package org.ReDiego0.combatSystem.listener

import org.ReDiego0.combatSystem.combat.TacticalDash
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DashInputListener(
    private val plugin: JavaPlugin,
    private val tacticalDash: TacticalDash
) : Listener {

    private val sneakingPlayers = ConcurrentHashMap<UUID, Boolean>()

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[DashInputListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerSneak(event: PlayerToggleSneakEvent) {
        val player = event.player

        if (player.gameMode != GameMode.SURVIVAL && player.gameMode != GameMode.ADVENTURE) return

        if (event.isSneaking) {
            sneakingPlayers[player.uniqueId] = true
        } else {
            sneakingPlayers.remove(player.uniqueId)
        }
    }

    fun isPlayerSneaking(player: Player): Boolean {
        return sneakingPlayers.getOrDefault(player.uniqueId, false) || player.isSneaking
    }

    fun tryDash(player: Player, direction: TacticalDash.DashDirection) {
        if (!isPlayerSneaking(player)) return
        if (!player.isJumping) return

        tacticalDash.executeDash(player, direction)
    }

    private val Player.isJumping: Boolean
        get() = velocity.y > 0.1 && !isOnGround

    fun removePlayer(player: Player) {
        sneakingPlayers.remove(player.uniqueId)
    }
}
