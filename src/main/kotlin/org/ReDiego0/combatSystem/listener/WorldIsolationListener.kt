package org.ReDiego0.combatSystem.listener

import org.ReDiego0.combatSystem.config.ConfigManager
import org.ReDiego0.combatSystem.world.WorldIsolation
import org.bukkit.GameMode
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin

class WorldIsolationListener(
    private val plugin: JavaPlugin,
    private val worldIsolation: WorldIsolation,
    private val configManager: ConfigManager
) : Listener {

    private val playersInSafeZones = mutableSetOf<Player>()

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[WorldIsolationListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val attacker = getAttacker(event.damager) ?: return
        val victim = event.entity

        if (attacker !is Player) return
        if (!isValidGameMode(attacker)) return

        val worldName = attacker.world.name
        if (!worldIsolation.isWorldEnabled(worldName)) {
            event.isCancelled = true
            if (configManager.isDebug()) {
                plugin.logger.info("[WorldIsolationListener] Combat cancelled in disabled world: $worldName")
            }
            return
        }

        if (configManager.isSafeZonesEnabled()) {
            if (isInSafeZone(attacker) && configManager.isCombatDisabledInSafeZone()) {
                event.isCancelled = true
                if (configManager.isDebug()) {
                    plugin.logger.info("[WorldIsolationListener] Combat cancelled - attacker in safe zone")
                }
                return
            }
            if (victim is Player && isInSafeZone(victim) && configManager.isCombatDisabledInSafeZone()) {
                event.isCancelled = true
                if (configManager.isDebug()) {
                    plugin.logger.info("[WorldIsolationListener] Combat cancelled - victim in safe zone")
                }
                return
            }
        }

        if (victim is Player && configManager.isDebug()) {
            plugin.logger.info("[WorldIsolationListener] PVP combat allowed between ${attacker.name} and ${victim.name}")
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (!isValidGameMode(player)) return

        val worldName = player.world.name
        if (!worldIsolation.isWorldEnabled(worldName)) {
            if (event.action.isRightClick) {
                event.isCancelled = true
                if (configManager.isDebug()) {
                    plugin.logger.info("[WorldIsolationListener] Interact cancelled in disabled world: $worldName")
                }
            }
            return
        }

        if (configManager.isSafeZonesEnabled() && isInSafeZone(player)) {
            if (configManager.isWeaponActivationDisabledInSafeZone() && event.action.isRightClick) {
                event.isCancelled = true
                if (configManager.isDebug()) {
                    plugin.logger.info("[WorldIsolationListener] Weapon activation cancelled in safe zone")
                }
            }
        }
    }

    fun markPlayerInSafeZone(player: Player) {
        playersInSafeZones.add(player)
        if (configManager.isDebug()) {
            plugin.logger.info("[WorldIsolationListener] Player ${player.name} entered safe zone")
        }
    }

    fun markPlayerLeftSafeZone(player: Player) {
        playersInSafeZones.remove(player)
        if (configManager.isDebug()) {
            plugin.logger.info("[WorldIsolationListener] Player ${player.name} left safe zone")
        }
    }

    fun isInSafeZone(player: Player): Boolean {
        return playersInSafeZones.contains(player)
    }

    fun clearSafeZones() {
        playersInSafeZones.clear()
    }

    private fun getAttacker(damager: Entity): Entity? {
        if (damager is org.bukkit.entity.Projectile) {
            val shooter = damager.shooter
            return if (shooter is Entity) shooter else null
        }
        if (damager is org.bukkit.entity.AreaEffectCloud) {
            return damager.source as? Entity
        }
        return damager
    }

    private fun isValidGameMode(player: Player): Boolean {
        return player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE
    }

    private fun validatePVPCombat(attacker: Player, victim: Player): Boolean {
        if (attacker.uniqueId == victim.uniqueId) {
            if (configManager.isDebug()) {
                plugin.logger.warning("[WorldIsolationListener] Blocked self-damage exploit by ${attacker.name}")
            }
            return false
        }
        if (!attacker.isOnline || !victim.isOnline) {
            return false
        }
        return true
    }
}
