package org.ReDiego0.combatSystem.core

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class StaminaManager {

    private val playerStamina = ConcurrentHashMap<UUID, Double>()
    private val maxStamina = 100.0
    private val regenRate = 5.0

    fun getStamina(player: Player): Double {
        return playerStamina.getOrDefault(player.uniqueId, maxStamina)
    }

    fun setStamina(player: Player, amount: Double) {
        playerStamina[player.uniqueId] = amount.coerceIn(0.0, maxStamina)
    }

    fun consumeStamina(player: Player, amount: Double): Boolean {
        val current = getStamina(player)
        if (current < amount) return false
        setStamina(player, current - amount)
        return true
    }

    fun regenerateStamina(player: Player) {
        val current = getStamina(player)
        if (current < maxStamina) {
            setStamina(player, (current + regenRate).coerceAtMost(maxStamina))
        }
    }

    fun getMaxStamina(): Double = maxStamina

    fun resetPlayer(player: Player) {
        playerStamina[player.uniqueId] = maxStamina
    }

    fun removePlayer(player: Player) {
        playerStamina.remove(player.uniqueId)
    }
}
