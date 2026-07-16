package org.ReDiego0.combatSystem.anti

import org.ReDiego0.combatSystem.core.PDCUtil
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DownscalingManager(private val plugin: JavaPlugin) {

    private val worldTiers = mutableMapOf<String, Int>()
    private val downscaledPlayers = ConcurrentHashMap<UUID, Map<String, Double>>()

    fun loadConfig(config: org.bukkit.configuration.ConfigurationSection) {
        worldTiers.clear()

        val tiersSection = config.getConfigurationSection("downscaling.world-tiers")
        if (tiersSection != null) {
            for (key in tiersSection.getKeys(false)) {
                worldTiers[key] = tiersSection.getInt(key)
            }
        }
    }

    fun shouldDownscale(player: Player, worldName: String): Boolean {
        val worldTier = getWorldTier(worldName) ?: return false
        val playerLevel = getPlayerMaxItemLevel(player)

        return playerLevel > worldTier + 5
    }

    fun getWorldTier(worldName: String): Int? {
        for ((pattern, tier) in worldTiers) {
            if (worldName.contains(pattern, ignoreCase = true)) {
                return tier
            }
        }
        return null
    }

    private fun getPlayerMaxItemLevel(player: Player): Int {
        var maxLevel = 0

        for (item in player.inventory.contents) {
            if (item != null && PDCUtil.hasCustomData(item)) {
                val level = PDCUtil.getInt(item, "level") ?: 0
                if (level > maxLevel) maxLevel = level
            }
        }

        val armorContents = player.inventory.armorContents
        for (item in armorContents) {
            if (item != null && PDCUtil.hasCustomData(item)) {
                val level = PDCUtil.getInt(item, "level") ?: 0
                if (level > maxLevel) maxLevel = level
            }
        }

        return maxLevel
    }

    fun getDownscaledStats(player: Player, worldName: String): Map<String, Double> {
        val worldTier = getWorldTier(worldName) ?: return emptyMap()
        val scaleFactor = worldTier.toDouble() / getPlayerMaxItemLevel(player).toDouble().coerceAtLeast(1.0)

        return mapOf(
            "damage_multiplier" to scaleFactor.coerceIn(0.1, 1.0),
            "defense_multiplier" to scaleFactor.coerceIn(0.1, 1.0),
            "speed_multiplier" to scaleFactor.coerceIn(0.5, 1.0)
        )
    }

    fun applyDownscaling(player: Player, worldName: String) {
        val stats = getDownscaledStats(player, worldName)
        downscaledPlayers[player.uniqueId] = stats

        if (plugin.config.getBoolean("debug", false)) {
            plugin.logger.info("[DownscalingManager] Applied downscaling to ${player.name} in $worldName")
        }
    }

    fun removeDownscaling(player: Player) {
        downscaledPlayers.remove(player.uniqueId)

        if (plugin.config.getBoolean("debug", false)) {
            plugin.logger.info("[DownscalingManager] Removed downscaling from ${player.name}")
        }
    }

    fun getDamageMultiplier(player: Player): Double {
        return downscaledPlayers[player.uniqueId]?.get("damage_multiplier") ?: 1.0
    }

    fun getDefenseMultiplier(player: Player): Double {
        return downscaledPlayers[player.uniqueId]?.get("defense_multiplier") ?: 1.0
    }

    fun isDownscaled(player: Player): Boolean {
        return downscaledPlayers.containsKey(player.uniqueId)
    }

    fun removePlayer(player: Player) {
        downscaledPlayers.remove(player.uniqueId)
    }
}
