package org.ReDiego0.combatSystem.config

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PVPConfig(private val plugin: JavaPlugin) {

    lateinit var config: YamlConfiguration
        private set
    private val configFile = File(plugin.dataFolder, "pvp.yml")

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

        yaml.set("pvp.enabled", true)

        yaml.set("pvp.xp.base-xp", 100)
        yaml.set("pvp.xp.opponent-level-multiplier-min", 0.5)
        yaml.set("pvp.xp.opponent-level-multiplier-max", 3.0)
        yaml.set("pvp.xp.anti-farm.enabled", true)
        yaml.set("pvp.xp.anti-farm.same-player-cooldown", 300)

        yaml.set("pvp.loot.enabled", true)
        yaml.set("pvp.loot.drop-on-death", true)
        yaml.set("pvp.loot.max-drops", 3)
        yaml.set("pvp.loot.exclude-slots", listOf(36, 37, 38, 39))

        yaml.set("pvp.towny.block-in-peaceful-towns", true)
        yaml.set("pvp.towny.allow-during-wars", true)

        yaml.save(configFile)
        plugin.logger.info("[PVPConfig] Default pvp.yml generated")
    }

    fun isEnabled(): Boolean {
        return config.getBoolean("pvp.enabled", true)
    }

    fun getBaseXP(): Double {
        return config.getDouble("pvp.xp.base-xp", 100.0)
    }

    fun getAntiFarmCooldown(): Long {
        return config.getLong("pvp.xp.anti-farm.same-player-cooldown", 300)
    }

    fun isLootEnabled(): Boolean {
        return config.getBoolean("pvp.loot.enabled", true)
    }

    fun getMaxDrops(): Int {
        return config.getInt("pvp.loot.max-drops", 3)
    }

    fun getExcludeSlots(): List<Int> {
        return config.getStringList("pvp.loot.exclude-slots").mapNotNull { it.toIntOrNull() }
    }

    fun isTownyIntegrationEnabled(): Boolean {
        return config.getBoolean("pvp.towny.block-in-peaceful-towns", true)
    }

    fun isWarPVPAllowed(): Boolean {
        return config.getBoolean("pvp.towny.allow-during-wars", true)
    }
}
