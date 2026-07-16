package org.ReDiego0.combatSystem.anti

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BossDamageTracker {

    private val bossHealth = ConcurrentHashMap<UUID, Double>()
    private val playerDamage = ConcurrentHashMap<UUID, MutableMap<UUID, Double>>()
    private val minDamagePercentage = 0.05

    fun startTracking(boss: LivingEntity, maxHealth: Double) {
        bossHealth[boss.uniqueId] = maxHealth
        playerDamage[boss.uniqueId] = mutableMapOf()
    }

    fun addDamage(boss: LivingEntity, player: Player, damage: Double) {
        val bossId = boss.uniqueId
        val playerId = player.uniqueId

        playerDamage.getOrPut(bossId) { mutableMapOf() }
        val currentDamage = playerDamage[bossId]?.get(playerId) ?: 0.0
        playerDamage[bossId]?.put(playerId, currentDamage + damage)
    }

    fun qualifiesForLoot(boss: LivingEntity, player: Player): Boolean {
        val bossId = boss.uniqueId
        val playerId = player.uniqueId

        val maxHealth = bossHealth[bossId] ?: return true
        val damageDealt = playerDamage[bossId]?.get(playerId) ?: 0.0

        val minDamage = maxHealth * minDamagePercentage
        return damageDealt >= minDamage
    }

    fun getDamagePercentage(boss: LivingEntity, player: Player): Double {
        val bossId = boss.uniqueId
        val playerId = player.uniqueId

        val maxHealth = bossHealth[bossId] ?: return 0.0
        val damageDealt = playerDamage[bossId]?.get(playerId) ?: 0.0

        return damageDealt / maxHealth
    }

    fun cleanup(boss: LivingEntity) {
        val bossId = boss.uniqueId
        bossHealth.remove(bossId)
        playerDamage.remove(bossId)
    }

    fun clearAll() {
        bossHealth.clear()
        playerDamage.clear()
    }

    fun isTracking(boss: LivingEntity): Boolean {
        return bossHealth.containsKey(boss.uniqueId)
    }

    fun setMinDamagePercentage(percentage: Double) {
        // This could be made configurable
    }
}
