package org.ReDiego0.combatSystem.data

data class ArmorSet(
    val setId: String,
    val equippedPieces: List<ArmorData>
) {
    fun getPieceCount(): Int = equippedPieces.size

    fun getSynergyMultiplier(): Double {
        return when (equippedPieces.size) {
            2 -> 1.25
            3 -> 1.50
            4 -> 2.00
            else -> 1.0
        }
    }

    fun getDurationBonus(): Double {
        return if (equippedPieces.size >= 4) 2.0 else 0.0
    }

    fun hasBonus(): Boolean = equippedPieces.size >= 2
}
