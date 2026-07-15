package org.ReDiego0.combatSystem.combat

import org.ReDiego0.combatSystem.core.StaminaManager
import org.ReDiego0.combatSystem.world.TownyIntegration
import org.ReDiego0.combatSystem.world.WorldIsolation
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TacticalDash(
    private val worldIsolation: WorldIsolation,
    private val townyIntegration: TownyIntegration,
    private val staminaManager: StaminaManager
) {

    private val dashCooldowns = ConcurrentHashMap<UUID, Long>()
    private val cooldownMs = 1500L
    private val staminaCost = 30.0

    private val forwardDistance = 3.0
    private val backwardDistance = 2.0
    private val lateralDistance = 2.0

    fun canDash(player: Player): Boolean {
        if (!worldIsolation.isWorldEnabled(player.world.name)) return false
        if (!townyIntegration.canUseCombatAbilities(player)) return false

        val lastDash = dashCooldowns[player.uniqueId] ?: 0L
        if (System.currentTimeMillis() - lastDash < cooldownMs) return false

        if (staminaManager.getStamina(player) < staminaCost) return false

        return true
    }

    fun executeDash(player: Player, direction: DashDirection) {
        if (!canDash(player)) return

        staminaManager.consumeStamina(player, staminaCost)
        dashCooldowns[player.uniqueId] = System.currentTimeMillis()

        val vector = when (direction) {
            DashDirection.FORWARD -> player.location.direction.normalize().multiply(forwardDistance)
            DashDirection.BACKWARD -> player.location.direction.normalize().multiply(-backwardDistance)
            DashDirection.LEFT -> {
                val dir = player.location.direction.normalize()
                val left = Vector(-dir.z, 0.0, dir.x).normalize().multiply(lateralDistance)
                left
            }
            DashDirection.RIGHT -> {
                val dir = player.location.direction.normalize()
                val right = Vector(dir.z, 0.0, -dir.x).normalize().multiply(lateralDistance)
                right
            }
        }

        vector.y = 0.2
        player.velocity = vector

        player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f)
    }

    fun getCooldownRemaining(player: Player): Long {
        val lastDash = dashCooldowns[player.uniqueId] ?: 0L
        val elapsed = System.currentTimeMillis() - lastDash
        return if (elapsed < cooldownMs) cooldownMs - elapsed else 0L
    }

    fun isOnCooldown(player: Player): Boolean {
        return getCooldownRemaining(player) > 0L
    }

    fun removePlayer(player: Player) {
        dashCooldowns.remove(player.uniqueId)
    }

    enum class DashDirection {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT
    }
}
