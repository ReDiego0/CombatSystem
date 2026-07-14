package org.ReDiego0.combatSystem.data

data class WeaponData(
    val id: String,
    val displayName: String,
    val category: WeaponCategory,
    val rarity: Rarity,
    val tier: Int,
    val baseDamage: Double,
    val attackSpeed: Double,
    val traitPool: String?,
    val lootPools: List<String>
)
