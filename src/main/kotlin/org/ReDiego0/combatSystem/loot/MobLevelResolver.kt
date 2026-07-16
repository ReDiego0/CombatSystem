package org.ReDiego0.combatSystem.loot

import org.ReDiego0.combatSystem.world.RegionChecker
import org.bukkit.entity.EntityType
import org.bukkit.plugin.java.JavaPlugin

class MobLevelResolver(
    private val plugin: JavaPlugin,
    private val regionChecker: RegionChecker
) {

    private val mobOverrides = mutableMapOf<EntityType, Int>()
    private val regionOverrides = mutableMapOf<String, Int>()
    private val worldTiers = mutableMapOf<String, Int>()
    private var defaultLevel = 1

    fun load(config: org.bukkit.configuration.ConfigurationSection) {
        mobOverrides.clear()
        regionOverrides.clear()
        worldTiers.clear()

        val overridesSection = config.getConfigurationSection("mob-levels.overrides")
        if (overridesSection != null) {
            for (key in overridesSection.getKeys(false)) {
                try {
                    val entityType = EntityType.valueOf(key.uppercase())
                    mobOverrides[entityType] = overridesSection.getInt(key)
                } catch (e: Exception) {
                    plugin.logger.warning("[MobLevelResolver] Invalid mob type: $key")
                }
            }
        }

        val regionSection = config.getConfigurationSection("mob-levels.region-overrides")
        if (regionSection != null) {
            for (key in regionSection.getKeys(false)) {
                regionOverrides[key] = regionSection.getInt(key)
            }
        }

        val worldSection = config.getConfigurationSection("mob-levels.world-tiers")
        if (worldSection != null) {
            for (key in worldSection.getKeys(false)) {
                worldTiers[key] = worldSection.getInt(key)
            }
        }

        defaultLevel = config.getInt("mob-levels.default", 1)
    }

    fun resolveLevel(entityType: EntityType, worldName: String, location: org.bukkit.Location): Int {
        mobOverrides[entityType]?.let { return it }

        val region = regionChecker.getRegionAt(location)
        if (region != null) {
            regionOverrides[region]?.let { return it }
        }

        for ((pattern, tier) in worldTiers) {
            if (worldName.contains(pattern, ignoreCase = true)) {
                return tier
            }
        }

        return defaultLevel
    }
}
