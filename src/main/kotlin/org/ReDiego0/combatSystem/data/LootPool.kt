package org.ReDiego0.combatSystem.data

data class LootPool(
    val id: String,
    val displayName: String,
    val description: String,
    val conditions: LootConditions,
    val drops: List<LootDrop>
) {
    data class LootConditions(
        val worlds: List<String>,
        val mobs: List<String>,
        val minEnemyLevel: Int?,
        val maxEnemyLevel: Int?,
        val biomes: List<String>,
        val timeOfDay: TimeRange?,
        val requiredPermission: String?,
        val minPlayerLevel: Int?,
        val maxPlayerLevel: Int?,
        val requiredWorldGuardRegion: String?,
        val chance: Double?
    ) {
        data class TimeRange(val min: Int, val max: Int)
    }

    data class LootDrop(
        val weaponId: String,
        val weight: Int
    )
}
