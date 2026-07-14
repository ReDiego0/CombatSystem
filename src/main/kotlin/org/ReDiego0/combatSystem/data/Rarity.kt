package org.ReDiego0.combatSystem.data

enum class Rarity(
    val displayName: String,
    val colorCode: String,
    val loreSymbol: String,
    val maxTier: Int
) {
    COMMON("Common", "§f", "◆", 0),
    UNCOMMON("Uncommon", "§a", "◆", 0),
    RARE("Rare", "§9", "◆", 0),
    LEGENDARY("Legendary", "§6", "★", 5),
    MYTHIC("Mythic", "§c", "✦", 5);

    fun canHaveTier(): Boolean = maxTier > 0

    companion object {
        fun fromString(value: String): Rarity? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
