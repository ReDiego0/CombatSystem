package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.data.ArmorBonus
import org.ReDiego0.combatSystem.data.PassiveTrigger
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ArmorPassiveManager(
    private val armorRegistry: ArmorRegistry,
    private val bonusLoader: ArmorBonusLoader,
    private val setBonusCalculator: SetBonusCalculator
) {

    private val activeEffects = ConcurrentHashMap<UUID, MutableMap<String, Long>>()

    fun onTrigger(player: Player, trigger: PassiveTrigger) {
        val equippedArmor = armorRegistry.getEquippedArmor(player)
        val sets = armorRegistry.getEquippedSets(player)

        for ((_, armorData) in equippedArmor) {
            if (armorData?.bonusId == null) continue

            val bonus = bonusLoader.get(armorData.bonusId) ?: continue
            if (bonus.trigger != trigger) continue

            if (bonus.bonusType == ArmorBonus.BonusType.CONDITIONAL) {
                if (!checkCondition(player, bonus)) continue
            }

            val setForArmor = sets.find { it.setId == armorData.setId }
            val effectiveBonus = if (setForArmor != null) {
                setBonusCalculator.calculateEffectiveBonus(bonus, setForArmor)
            } else {
                bonus
            }

            applyEffect(player, effectiveBonus)
        }
    }

    private fun checkCondition(player: Player, bonus: ArmorBonus): Boolean {
        when (bonus.trigger) {
            PassiveTrigger.LOW_HP -> {
                val threshold = bonus.getConditionHPThreshold()
                val healthPercentage = player.health / player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)!!.value
                return healthPercentage <= threshold
            }
            else -> return true
        }
    }

    private fun applyEffect(player: Player, bonus: ArmorBonus) {
        val effectType = bonus.getEffectType()
        val value = bonus.getEffectValue()
        val duration = (bonus.getEffectDuration() * 20).toLong()

        when (effectType) {
            "ATTACK_SPEED_BOOST" -> {
                val level = (value * 10).toInt().coerceIn(1, 5)
                player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, duration.toInt(), level))
            }
            "DAMAGE_REDUCTION" -> {
                val level = (value * 10).toInt().coerceIn(1, 5)
                player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, duration.toInt(), level))
            }
            "SPEED_BOOST" -> {
                val level = (value * 10).toInt().coerceIn(1, 5)
                player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, duration.toInt(), level))
            }
            "LIFESTEAL_BOOST" -> {
                val effectKey = "${bonus.id}_lifesteal"
                activeEffects.getOrPut(player.uniqueId) { mutableMapOf() }[effectKey] =
                    System.currentTimeMillis() + (duration * 50)
            }
            "STRENGTH_BOOST" -> {
                val level = (value * 10).toInt().coerceIn(1, 5)
                player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, duration.toInt(), level))
            }
        }
    }

    fun hasLifestealBonus(player: Player): Boolean {
        val effects = activeEffects[player.uniqueId] ?: return false
        val now = System.currentTimeMillis()
        return effects.values.any { it > now }
    }

    fun getLifestealPercentage(player: Player): Double {
        val effects = activeEffects[player.uniqueId] ?: return 0.0
        val now = System.currentTimeMillis()
        var total = 0.0

        for ((key, endTime) in effects) {
            if (endTime > now && key.endsWith("_lifesteal")) {
                total += 0.05
            }
        }

        return total
    }

    fun cleanupExpiredEffects(player: Player) {
        val effects = activeEffects[player.uniqueId] ?: return
        val now = System.currentTimeMillis()
        effects.entries.removeIf { it.value <= now }
    }

    fun removePlayer(player: Player) {
        activeEffects.remove(player.uniqueId)
    }
}
