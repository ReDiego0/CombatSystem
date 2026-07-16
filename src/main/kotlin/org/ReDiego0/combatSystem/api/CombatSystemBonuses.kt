package org.ReDiego0.combatSystem.api

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object CombatSystemBonuses {

    private val xpBonuses = ConcurrentHashMap<UUID, BonusData>()
    private val lootChanceBonuses = ConcurrentHashMap<UUID, BonusData>()
    private val damageBonuses = ConcurrentHashMap<UUID, BonusData>()

    data class BonusData(
        val multiplier: Double,
        val expiryTime: Long
    )

    fun setXPBonus(player: Player, multiplier: Double, durationSeconds: Long) {
        val expiryTime = System.currentTimeMillis() + (durationSeconds * 1000)
        xpBonuses[player.uniqueId] = BonusData(multiplier, expiryTime)
    }

    fun setLootChanceBonus(player: Player, multiplier: Double, durationSeconds: Long) {
        val expiryTime = System.currentTimeMillis() + (durationSeconds * 1000)
        lootChanceBonuses[player.uniqueId] = BonusData(multiplier, expiryTime)
    }

    fun setDamageBonus(player: Player, multiplier: Double, durationSeconds: Long) {
        val expiryTime = System.currentTimeMillis() + (durationSeconds * 1000)
        damageBonuses[player.uniqueId] = BonusData(multiplier, expiryTime)
    }

    fun clearAllBonuses(player: Player) {
        xpBonuses.remove(player.uniqueId)
        lootChanceBonuses.remove(player.uniqueId)
        damageBonuses.remove(player.uniqueId)
    }

    fun getXPBonus(player: Player): Double {
        val bonus = xpBonuses[player.uniqueId] ?: return 1.0
        if (isExpired(bonus)) {
            xpBonuses.remove(player.uniqueId)
            return 1.0
        }
        return bonus.multiplier
    }

    fun getLootChanceBonus(player: Player): Double {
        val bonus = lootChanceBonuses[player.uniqueId] ?: return 1.0
        if (isExpired(bonus)) {
            lootChanceBonuses.remove(player.uniqueId)
            return 1.0
        }
        return bonus.multiplier
    }

    fun getDamageBonus(player: Player): Double {
        val bonus = damageBonuses[player.uniqueId] ?: return 1.0
        if (isExpired(bonus)) {
            damageBonuses.remove(player.uniqueId)
            return 1.0
        }
        return bonus.multiplier
    }

    private fun isExpired(bonus: BonusData): Boolean {
        return System.currentTimeMillis() > bonus.expiryTime
    }

    fun cleanupExpired() {
        val now = System.currentTimeMillis()
        xpBonuses.entries.removeIf { it.value.expiryTime < now }
        lootChanceBonuses.entries.removeIf { it.value.expiryTime < now }
        damageBonuses.entries.removeIf { it.value.expiryTime < now }
    }

    fun removePlayer(player: Player) {
        xpBonuses.remove(player.uniqueId)
        lootChanceBonuses.remove(player.uniqueId)
        damageBonuses.remove(player.uniqueId)
    }
}
