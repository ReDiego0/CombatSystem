package org.ReDiego0.combatSystem.pvp

import org.ReDiego0.combatSystem.core.PDCUtil
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class PVPXPCalculator(private val plugin: JavaPlugin) {

    private var baseXP = 100.0
    private var minMultiplier = 0.5
    private var maxMultiplier = 3.0

    fun loadConfig(config: org.bukkit.configuration.ConfigurationSection) {
        baseXP = config.getDouble("xp.base-xp", 100.0)
        minMultiplier = config.getDouble("xp.opponent-level-multiplier-min", 0.5)
        maxMultiplier = config.getDouble("xp.opponent-level-multiplier-max", 3.0)
    }

    fun calculateXP(killer: Player, victim: Player): Double {
        val opponentLevel = getPlayerLevel(victim)
        val levelMultiplier = (opponentLevel / 10.0).coerceIn(minMultiplier, maxMultiplier)

        return baseXP * levelMultiplier
    }

    private fun getPlayerLevel(player: Player): Int {
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
}
