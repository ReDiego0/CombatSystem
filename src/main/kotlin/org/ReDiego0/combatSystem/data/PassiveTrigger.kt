package org.ReDiego0.combatSystem.data

enum class PassiveTrigger {
    FIRE_DAMAGE_TAKEN,
    LOW_HP,
    ENEMY_KILLED,
    DASH_USED,
    SKILL_USED,
    CRITICAL_HIT,
    HIT_TAKEN,
    HIT_DEALT;

    companion object {
        fun fromString(value: String): PassiveTrigger? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
