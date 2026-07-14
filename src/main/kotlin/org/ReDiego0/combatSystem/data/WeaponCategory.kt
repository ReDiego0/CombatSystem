package org.ReDiego0.combatSystem.data

import org.bukkit.Material

enum class WeaponCategory(
    val displayName: String,
    val material: Material,
    val attackSpeed: Double,
    val baseDamage: Double,
    val skill1Name: String,
    val skill2Name: String
) {
    KATANA("Katana", Material.IRON_SWORD, 2.5, 8.0, "Swift Slash", "Blade Guard"),
    LONGSWORD("Longsword", Material.IRON_SWORD, 1.5, 14.0, "Lunge", "Parry"),
    HEAVY_HAMMER("Heavy Hammer", Material.MACE, 0.8, 22.0, "Ground Slam", "Iron Will"),
    CROSSBOW("Crossbow", Material.CROSSBOW, 1.0, 12.0, "Multi-Shot", "Net Shot"),
    SPEAR("Spear", Material.IRON_SPEAR, 1.8, 12.0, "Impale", "Sweep"),
    STAFF("Staff", Material.TRIDENT, 1.0, 6.0, "Arcane Burst", "Barrier"),
    BOW("Bow", Material.BOW, 1.2, 10.0, "Rapid Shot", "Explosive Arrow");

    companion object {
        fun fromString(value: String): WeaponCategory? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
