package org.ReDiego0.combatSystem.loot

import org.ReDiego0.combatSystem.data.LootPool
import kotlin.random.Random

class LootRoller {

    fun roll(pool: LootPool): LootPool.LootDrop? {
        if (pool.drops.isEmpty()) return null

        val totalWeight = pool.drops.sumOf { it.weight }
        if (totalWeight <= 0) return null

        val roll = Random.nextInt(totalWeight)
        var current = 0

        for (drop in pool.drops) {
            current += drop.weight
            if (roll < current) {
                return drop
            }
        }

        return pool.drops.last()
    }

    fun rollMultiple(pool: LootPool, count: Int): List<LootPool.LootDrop> {
        val results = mutableListOf<LootPool.LootDrop>()
        repeat(count) {
            val drop = roll(pool)
            if (drop != null) {
                results.add(drop)
            }
        }
        return results
    }

    fun shouldDrop(pool: LootPool): Boolean {
        val chance = pool.conditions.chance ?: 1.0
        return Random.nextDouble() < chance
    }
}
