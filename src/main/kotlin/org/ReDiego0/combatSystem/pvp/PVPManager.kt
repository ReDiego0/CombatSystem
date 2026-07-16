package org.ReDiego0.combatSystem.pvp

import org.ReDiego0.combatSystem.item.WeaponRegistry
import org.ReDiego0.combatSystem.loadout.LoadoutManager
import org.ReDiego0.combatSystem.loot.LootDelivery
import org.ReDiego0.combatSystem.loot.LootEngine
import org.ReDiego0.combatSystem.loot.LootNotification
import org.ReDiego0.combatSystem.loot.LootRoller
import org.ReDiego0.combatSystem.world.TownyIntegration
import org.ReDiego0.combatSystem.world.WorldIsolation
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PVPManager(
    private val plugin: JavaPlugin,
    private val pvpXPCalculator: PVPXPCalculator,
    private val pvpLootLoader: PVPLootLoader,
    private val lootRoller: LootRoller,
    private val lootDelivery: LootDelivery,
    private val lootNotification: LootNotification,
    private val worldIsolation: WorldIsolation,
    private val townyIntegration: TownyIntegration
) {

    private val antiFarmCooldowns = ConcurrentHashMap<UUID, MutableMap<UUID, Long>>()
    private var antiFarmCooldownSeconds = 300L

    fun loadConfig(config: org.bukkit.configuration.ConfigurationSection) {
        antiFarmCooldownSeconds = config.getLong("xp.anti-farm.same-player-cooldown", 300)
        pvpXPCalculator.loadConfig(config)
    }

    fun onPlayerKill(killer: Player, victim: Player) {
        if (!canGetPVPXP(killer, victim)) return

        val xp = pvpXPCalculator.calculateXP(killer, victim)

        val bonusMultiplier = org.ReDiego0.combatSystem.api.CombatSystemBonuses.getXPBonus(killer)
        val finalXP = xp * bonusMultiplier

        setAntiFarmCooldown(killer, victim)

        val mainHand = killer.inventory.itemInMainHand
        if (org.ReDiego0.combatSystem.item.ItemFactory.isManagedWeapon(mainHand)) {
            val updatedWeapon = org.ReDiego0.combatSystem.item.ItemProgression(
                org.ReDiego0.combatSystem.config.ProgressionConfig(plugin)
            ).addXP(mainHand, finalXP)
            killer.inventory.setItemInMainHand(updatedWeapon)

            if (plugin.config.getBoolean("debug", false)) {
                plugin.logger.info("[PVPManager] ${killer.name} gained ${"%.0f".format(finalXP)} XP from killing ${victim.name}")
            }
        }

        evaluatePVPLoot(killer, victim)
    }

    private fun canGetPVPXP(killer: Player, victim: Player): Boolean {
        if (!worldIsolation.isWorldEnabled(killer.world.name)) return false

        if (townyIntegration.isAvailable() && townyIntegration.isPlayerInCity(killer)) {
            if (!townyIntegration.isNationAtWar(killer)) return false
        }

        if (isOnAntiFarmCooldown(killer, victim)) return false

        return true
    }

    private fun isOnAntiFarmCooldown(killer: Player, victim: Player): Boolean {
        val killerCooldowns = antiFarmCooldowns[killer.uniqueId] ?: return false
        val lastKillTime = killerCooldowns[victim.uniqueId] ?: return false
        val elapsed = System.currentTimeMillis() - lastKillTime
        return elapsed < (antiFarmCooldownSeconds * 1000)
    }

    private fun setAntiFarmCooldown(killer: Player, victim: Player) {
        antiFarmCooldowns.getOrPut(killer.uniqueId) { mutableMapOf() }
        antiFarmCooldowns[killer.uniqueId]?.put(victim.uniqueId, System.currentTimeMillis())
    }

    private fun evaluatePVPLoot(killer: Player, victim: Player) {
        val eligiblePools = mutableListOf<org.ReDiego0.combatSystem.data.LootPool>()

        for (pool in pvpLootLoader.getAllPools().values) {
            if (!matchesWorld(pool, killer.world.name)) continue
            eligiblePools.add(pool)
        }

        for (pool in eligiblePools) {
            if (!lootRoller.shouldDrop(pool)) continue

            val drop = lootRoller.roll(pool) ?: continue
            lootDelivery.deliverConsumable(killer, drop.weaponId)
            lootNotification.sendConsumableNotification(killer, drop.weaponId)
        }
    }

    private fun matchesWorld(pool: org.ReDiego0.combatSystem.data.LootPool, worldName: String): Boolean {
        if (pool.conditions.worlds.isEmpty()) return true
        return pool.conditions.worlds.any { pattern ->
            worldName.contains(pattern, ignoreCase = true)
        }
    }

    fun clearPlayer(player: Player) {
        antiFarmCooldowns.remove(player.uniqueId)
    }

    fun clearAll() {
        antiFarmCooldowns.clear()
    }
}
