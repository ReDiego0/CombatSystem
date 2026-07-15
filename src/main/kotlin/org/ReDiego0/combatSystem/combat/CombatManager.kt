package org.ReDiego0.combatSystem.combat

import org.ReDiego0.combatSystem.data.WeaponCategory
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CombatManager {

    private val cooldowns = ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>>()

    fun isOnCooldown(player: Player, skillId: String): Boolean {
        val playerCooldowns = cooldowns[player.uniqueId] ?: return false
        val endTime = playerCooldowns[skillId] ?: return false
        return System.currentTimeMillis() < endTime
    }

    fun getCooldownRemaining(player: Player, skillId: String): Double {
        val playerCooldowns = cooldowns[player.uniqueId] ?: return 0.0
        val endTime = playerCooldowns[skillId] ?: return 0.0
        val remaining = endTime - System.currentTimeMillis()
        return if (remaining > 0) remaining / 1000.0 else 0.0
    }

    fun setCooldown(player: Player, skillId: String, cooldownSeconds: Double) {
        val playerCooldowns = cooldowns.getOrPut(player.uniqueId) { ConcurrentHashMap() }
        playerCooldowns[skillId] = System.currentTimeMillis() + (cooldownSeconds * 1000).toLong()
    }

    fun getSkill1Name(category: WeaponCategory): String {
        return category.skill1Name
    }

    fun getSkill2Name(category: WeaponCategory): String {
        return category.skill2Name
    }

    fun getSkill1Cooldown(category: WeaponCategory): Double {
        return when (category) {
            WeaponCategory.KATANA -> 5.0
            WeaponCategory.LONGSWORD -> 6.0
            WeaponCategory.HEAVY_HAMMER -> 7.0
            WeaponCategory.CROSSBOW -> 5.0
            WeaponCategory.SPEAR -> 6.0
            WeaponCategory.STAFF -> 5.0
            WeaponCategory.BOW -> 4.0
        }
    }

    fun getSkill2Cooldown(category: WeaponCategory): Double {
        return when (category) {
            WeaponCategory.KATANA -> 6.0
            WeaponCategory.LONGSWORD -> 8.0
            WeaponCategory.HEAVY_HAMMER -> 10.0
            WeaponCategory.CROSSBOW -> 8.0
            WeaponCategory.SPEAR -> 7.0
            WeaponCategory.STAFF -> 12.0
            WeaponCategory.BOW -> 6.0
        }
    }

    fun getSkill1StaminaCost(category: WeaponCategory): Double {
        return when (category) {
            WeaponCategory.KATANA -> 20.0
            WeaponCategory.LONGSWORD -> 20.0
            WeaponCategory.HEAVY_HAMMER -> 25.0
            WeaponCategory.CROSSBOW -> 15.0
            WeaponCategory.SPEAR -> 20.0
            WeaponCategory.STAFF -> 20.0
            WeaponCategory.BOW -> 15.0
        }
    }

    fun getSkill2StaminaCost(category: WeaponCategory): Double {
        return when (category) {
            WeaponCategory.KATANA -> 25.0
            WeaponCategory.LONGSWORD -> 25.0
            WeaponCategory.HEAVY_HAMMER -> 30.0
            WeaponCategory.CROSSBOW -> 25.0
            WeaponCategory.SPEAR -> 25.0
            WeaponCategory.STAFF -> 30.0
            WeaponCategory.BOW -> 20.0
        }
    }

    fun removePlayer(player: Player) {
        cooldowns.remove(player.uniqueId)
    }
}
