package org.ReDiego0.combatSystem.data

data class ArmorBonus(
    val id: String,
    val displayName: String,
    val description: String,
    val bonusType: BonusType,
    val trigger: PassiveTrigger,
    val condition: Map<String, Any>?,
    val effect: Map<String, Any>
) {
    enum class BonusType {
        PASSIVE,
        CONDITIONAL
    }

    fun getEffectType(): String {
        return effect["type"]?.toString() ?: "NONE"
    }

    fun getEffectValue(default: Double = 0.0): Double {
        return (effect["value"] as? Number)?.toDouble() ?: default
    }

    fun getEffectDuration(default: Double = 0.0): Double {
        return (effect["duration"] as? Number)?.toDouble() ?: default
    }

    fun getConditionHPThreshold(default: Double = 0.5): Double {
        return (condition?.get("hp-threshold") as? Number)?.toDouble() ?: default
    }
}
