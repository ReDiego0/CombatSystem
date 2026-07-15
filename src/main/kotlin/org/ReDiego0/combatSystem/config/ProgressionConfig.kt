package org.ReDiego0.combatSystem.config

import org.ReDiego0.combatSystem.data.Rarity
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class ProgressionConfig(private val plugin: JavaPlugin) {

    private lateinit var config: YamlConfiguration
    private val configFile = File(plugin.dataFolder, "progression.yml")

    fun load() {
        plugin.dataFolder.mkdirs()
        if (!configFile.exists()) {
            generateDefault()
        }
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun reload() {
        load()
    }

    private fun generateDefault() {
        val yaml = YamlConfiguration()

        yaml.set("item-progression.max-level", 40)

        yaml.set("item-progression.xp-curve.base", 100)
        yaml.set("item-progression.xp-curve.exponent", 1.5)

        yaml.set("item-progression.xp-sources.mob-kill.base-xp", 10)

        yaml.set("item-progression.stat-scaling.damage-growth-per-level", 0.5)
        yaml.set("item-progression.stat-scaling.attack-speed-growth-per-level", 0.02)

        yaml.set("item-progression.stat-scaling.rarity-multipliers.COMMON", 1.0)
        yaml.set("item-progression.stat-scaling.rarity-multipliers.UNCOMMON", 1.15)
        yaml.set("item-progression.stat-scaling.rarity-multipliers.RARE", 1.35)
        yaml.set("item-progression.stat-scaling.rarity-multipliers.LEGENDARY", 1.6)
        yaml.set("item-progression.stat-scaling.rarity-multipliers.MYTHIC", 2.0)

        yaml.set("item-progression.xp-distribution.weapon-share", 0.5)
        yaml.set("item-progression.xp-distribution.armor-share-per-piece", 0.125)

        yaml.save(configFile)
        plugin.logger.info("[ProgressionConfig] Default progression.yml generated")
    }

    fun getMaxLevel(): Int {
        return config.getInt("item-progression.max-level", 40)
    }

    fun getXPBase(): Double {
        return config.getDouble("item-progression.xp-curve.base", 100.0)
    }

    fun getXPExponent(): Double {
        return config.getDouble("item-progression.xp-curve.exponent", 1.5)
    }

    fun getMobKillBaseXP(): Double {
        return config.getDouble("item-progression.xp-sources.mob-kill.base-xp", 10.0)
    }

    fun getDamageGrowth(): Double {
        return config.getDouble("item-progression.stat-scaling.damage-growth-per-level", 0.5)
    }

    fun getAttackSpeedGrowth(): Double {
        return config.getDouble("item-progression.stat-scaling.attack-speed-growth-per-level", 0.02)
    }

    fun getRarityMultiplier(rarity: Rarity): Double {
        return config.getDouble(
            "item-progression.stat-scaling.rarity-multipliers.${rarity.name}",
            1.0
        )
    }

    fun getWeaponShare(): Double {
        return config.getDouble("item-progression.xp-distribution.weapon-share", 0.5)
    }

    fun getArmorSharePerPiece(): Double {
        return config.getDouble("item-progression.xp-distribution.armor-share-per-piece", 0.125)
    }
}
