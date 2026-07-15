package org.ReDiego0.combatSystem.data

data class Trait(
    val id: String,
    val displayName: String,
    val description: String,
    val categories: List<WeaponCategory>,
    val effects: List<TraitEffect>,
    val visual: TraitVisual
)
