package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.data.ArmorBonus
import org.ReDiego0.combatSystem.data.ArmorSet

class SetBonusCalculator {

    fun calculateEffectiveBonus(bonus: ArmorBonus, set: ArmorSet): ArmorBonus {
        if (!set.hasBonus()) return bonus

        val multiplier = set.getSynergyMultiplier()
        val durationBonus = set.getDurationBonus()

        val currentValue = bonus.getEffectValue()
        val currentDuration = bonus.getEffectDuration()

        val newValue = currentValue * multiplier
        val newDuration = currentDuration + durationBonus

        val newEffect = bonus.effect.toMutableMap()
        newEffect["value"] = newValue
        newEffect["duration"] = newDuration

        return bonus.copy(effect = newEffect)
    }

    fun getSetDescription(set: ArmorSet): String {
        val count = set.getPieceCount()
        val multiplier = set.getSynergyMultiplier()
        val durationBonus = set.getDurationBonus()

        return buildString {
            append("§6${set.setId} §7($count/4 pieces)")
            if (set.hasBonus()) {
                append("\n§7Synergy: §a+${((multiplier - 1) * 100).toInt()}% power")
                if (durationBonus > 0) {
                    append(" §7| §a+${durationBonus.toInt()}s duration")
                }
            }
        }
    }
}
