package org.ReDiego0.combatSystem.loot

import org.ReDiego0.combatSystem.data.LootPool
import org.ReDiego0.combatSystem.item.PoolRegistry
import org.ReDiego0.combatSystem.world.RegionChecker
import org.ReDiego0.combatSystem.world.WorldIsolation
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class LootEngine(
    private val plugin: JavaPlugin,
    private val poolRegistry: PoolRegistry,
    private val lootRoller: LootRoller,
    private val lootDelivery: LootDelivery,
    private val lootNotification: LootNotification,
    private val mobLevelResolver: MobLevelResolver,
    private val regionChecker: RegionChecker,
    private val worldIsolation: WorldIsolation
) {

    fun evaluateMobKill(player: Player, entityType: EntityType, location: Location) {
        val worldName = location.world?.name ?: return
        if (!worldIsolation.isWorldEnabled(worldName)) return

        val mobLevel = mobLevelResolver.resolveLevel(entityType, worldName, location)

        val eligiblePools = findEligiblePools(worldName, entityType, mobLevel, location)
        if (eligiblePools.isEmpty()) return

        for (pool in eligiblePools) {
            if (!lootRoller.shouldDrop(pool)) continue

            val drop = lootRoller.roll(pool) ?: continue
            processDrop(player, drop)
        }
    }

    private fun findEligiblePools(
        worldName: String,
        entityType: EntityType,
        mobLevel: Int,
        location: Location
    ): List<LootPool> {
        val eligible = mutableListOf<LootPool>()

        for (pool in poolRegistry.getAllLootPools().values) {
            if (!matchesWorld(pool, worldName)) continue
            if (!matchesRegion(pool, location)) continue
            if (!matchesMob(pool, entityType)) continue
            if (!matchesLevel(pool, mobLevel)) continue

            eligible.add(pool)
        }

        return eligible
    }

    private fun matchesWorld(pool: LootPool, worldName: String): Boolean {
        if (pool.conditions.worlds.isEmpty()) return true
        return pool.conditions.worlds.any { pattern ->
            worldName.contains(pattern, ignoreCase = true)
        }
    }

    private fun matchesRegion(pool: LootPool, location: Location): Boolean {
        if (pool.conditions.requiredWorldGuardRegion == null) return true
        if (!regionChecker.isAvailable()) return true

        return regionChecker.isInRegion(location, pool.conditions.requiredWorldGuardRegion!!)
    }

    private fun matchesMob(pool: LootPool, entityType: EntityType): Boolean {
        if (pool.conditions.mobs.isEmpty()) return true
        return pool.conditions.mobs.any { mobName ->
            mobName.equals(entityType.name, ignoreCase = true)
        }
    }

    private fun matchesLevel(pool: LootPool, mobLevel: Int): Boolean {
        val minLevel = pool.conditions.minEnemyLevel
        val maxLevel = pool.conditions.maxEnemyLevel

        if (minLevel != null && mobLevel < minLevel) return false
        if (maxLevel != null && mobLevel > maxLevel) return false

        return true
    }

    private fun processDrop(player: Player, drop: LootPool.LootDrop) {
        val weaponId = drop.weaponId
        val armorId = drop.weaponId
        val consumableId = drop.weaponId

        when {
            weaponId.startsWith("weapon_") || isWeapon(weaponId) -> {
                val success = lootDelivery.deliverWeapon(player, weaponId)
                if (success) {
                    val weaponData = plugin.server.servicesManager.getRegistration(
                        org.ReDiego0.combatSystem.item.WeaponRegistry::class.java
                    )?.provider?.get(weaponId)
                    if (weaponData != null) {
                        lootNotification.sendWeaponNotification(player, weaponData.displayName, weaponData.rarity)
                    }
                }
            }
            armorId.startsWith("armor_") || isArmor(armorId) -> {
                val success = lootDelivery.deliverArmor(player, armorId)
                if (success) {
                    lootNotification.sendArmorNotification(player, armorId, org.ReDiego0.combatSystem.data.Rarity.COMMON)
                }
            }
            else -> {
                val success = lootDelivery.deliverConsumable(player, consumableId)
                if (success) {
                    lootNotification.sendConsumableNotification(player, consumableId)
                }
            }
        }
    }

    private fun isWeapon(id: String): Boolean {
        return plugin.server.servicesManager.getRegistration(
            org.ReDiego0.combatSystem.item.WeaponRegistry::class.java
        )?.provider?.get(id) != null
    }

    private fun isArmor(id: String): Boolean {
        return plugin.server.servicesManager.getRegistration(
            org.ReDiego0.combatSystem.item.ArmorLoader::class.java
        )?.provider?.get(id) != null
    }
}
