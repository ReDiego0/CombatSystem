package org.ReDiego0.combatSystem.data

enum class EffectType(val groupName: String) {
    COMBO_DAMAGE("Combat"),
    CRITICAL_BOOST("Combat"),
    EXECUTE_DAMAGE("Combat"),
    BACKSTAB_DAMAGE("Combat"),
    CONSECUTIVE_DAMAGE("Combat"),
    FIRST_HIT_DAMAGE("Combat"),
    LIFESTEAL("Sustain"),
    HEAL_ON_KILL("Sustain"),
    HEAL_ON_HIT("Sustain"),
    DAMAGE_REDUCTION("Sustain"),
    THORNS("Sustain"),
    SLOW_ON_HIT("Control"),
    WEAKNESS_ON_HIT("Control"),
    STUN_ON_HIT("Control"),
    BLIND_ON_HIT("Control"),
    KNOCKBACK_ON_HIT("Control"),
    SPEED_ON_KILL("Mobility"),
    DASH_DAMAGE("Mobility"),
    EVADE_CHANCE("Mobility"),
    AOE_DAMAGE("AOE"),
    CHAIN_DAMAGE("AOE"),
    EXPLOSION_ON_KILL("AOE"),
    STAMINA_ON_HIT("Resource"),
    STAMINA_ON_KILL("Resource"),
    REDUCED_STAMINA_COST("Resource"),
    PASSIVE_DAMAGE("Passive"),
    PASSIVE_SPEED("Passive"),
    PASSIVE_ARMOR_PEN("Passive"),
    PASSIVE_DEFENSE("Passive");

    companion object {
        fun fromString(value: String): EffectType? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
