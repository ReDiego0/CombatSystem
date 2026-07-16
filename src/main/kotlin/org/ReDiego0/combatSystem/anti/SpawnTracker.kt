package org.ReDiego0.combatSystem.anti

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class SpawnTracker {

    private val spawnerMobs = ConcurrentHashMap<UUID, Boolean>()
    private val playerHits = ConcurrentHashMap<UUID, MutableSet<UUID>>()

    fun trackMob(entity: Entity, spawnReason: CreatureSpawnEvent.SpawnReason) {
        if (spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            spawnerMobs[entity.uniqueId] = true
        }
    }

    fun isSpawnerMob(entity: Entity): Boolean {
        return spawnerMobs.getOrDefault(entity.uniqueId, false)
    }

    fun markPlayerHit(entity: Entity, player: Player) {
        playerHits.getOrPut(entity.uniqueId) { mutableSetOf() }.add(player.uniqueId)
    }

    fun wasHitByPlayer(entity: Entity): Boolean {
        val hits = playerHits[entity.uniqueId]
        return hits != null && hits.isNotEmpty()
    }

    fun wasHitByPlayer(entity: Entity, player: Player): Boolean {
        val hits = playerHits[entity.uniqueId]
        return hits != null && hits.contains(player.uniqueId)
    }

    fun cleanup(entity: Entity) {
        spawnerMobs.remove(entity.uniqueId)
        playerHits.remove(entity.uniqueId)
    }

    fun clearAll() {
        spawnerMobs.clear()
        playerHits.clear()
    }
}
