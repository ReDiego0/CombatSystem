package org.ReDiego0.combatSystem.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class ConfigManager(private val plugin: JavaPlugin) {

    private lateinit var config: FileConfiguration
    private val configFile = File(plugin.dataFolder, "config.yml")

    fun load() {
        plugin.dataFolder.mkdirs()
        if (!configFile.exists()) {
            generateDefaultConfig()
        }
        config = YamlConfiguration.loadConfiguration(configFile)
        if (isDebug()) {
            plugin.logger.info("[ConfigManager] Configuration loaded successfully")
        }
    }

    fun reload() {
        load()
        plugin.logger.info("[ConfigManager] Configuration reloaded")
    }

    private fun generateDefaultConfig() {
        val defaultConfig = YamlConfiguration()

        defaultConfig.set("world-isolation.mode", "BLACKLIST")
        defaultConfig.set("world-isolation.worlds", listOf("lobby", "spawn", "creative", "hub"))

        defaultConfig.set("safe-zones.enabled", true)
        defaultConfig.set("safe-zones.worldguard-flag", "pvp-denied")
        defaultConfig.set("safe-zones.disable-combat", true)
        defaultConfig.set("safe-zones.disable-dash", true)
        defaultConfig.set("safe-zones.disable-skills", true)
        defaultConfig.set("safe-zones.disable-weapon-activation", true)

        defaultConfig.set("debug", false)

        defaultConfig.save(configFile)
        plugin.logger.info("[ConfigManager] Default configuration generated")
    }

    fun getIsolationMode(): IsolationMode {
        val modeStr = config.getString("world-isolation.mode", "BLACKLIST")
        return try {
            IsolationMode.valueOf(modeStr!!.uppercase())
        } catch (e: IllegalArgumentException) {
            plugin.logger.warning("[ConfigManager] Invalid isolation mode: $modeStr, defaulting to BLACKLIST")
            IsolationMode.BLACKLIST
        }
    }

    fun getWorldList(): List<String> {
        return config.getStringList("world-isolation.worlds")
    }

    fun isSafeZonesEnabled(): Boolean {
        return config.getBoolean("safe-zones.enabled", true)
    }

    fun getSafeZoneFlag(): String {
        return config.getString("safe-zones.worldguard-flag", "pvp-denied") ?: "pvp-denied"
    }

    fun isCombatDisabledInSafeZone(): Boolean {
        return config.getBoolean("safe-zones.disable-combat", true)
    }

    fun isDashDisabledInSafeZone(): Boolean {
        return config.getBoolean("safe-zones.disable-dash", true)
    }

    fun isSkillsDisabledInSafeZone(): Boolean {
        return config.getBoolean("safe-zones.disable-skills", true)
    }

    fun isWeaponActivationDisabledInSafeZone(): Boolean {
        return config.getBoolean("safe-zones.disable-weapon-activation", true)
    }

    fun isDebug(): Boolean {
        return config.getBoolean("debug", false)
    }

    enum class IsolationMode {
        WHITELIST,
        BLACKLIST
    }
}
