package org.ReDiego0.combatSystem.data

import org.bukkit.Material

data class ArmorData(
    val id: String,
    val displayName: String,
    val material: Material,
    val setId: String?,
    val setPiece: ArmorSlot?,
    val rarity: Rarity,
    val tier: Int,
    val defense: Double,
    val hpBonus: Double,
    val bonusId: String?
) {
    enum class ArmorSlot {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS;

        companion object {
            fun fromString(value: String): ArmorSlot? {
                return entries.find { it.name.equals(value, ignoreCase = true) }
            }
        }
    }
}
