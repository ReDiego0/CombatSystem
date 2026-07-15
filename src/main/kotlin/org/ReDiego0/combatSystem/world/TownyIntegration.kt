package org.ReDiego0.combatSystem.world

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class TownyIntegration(private val plugin: JavaPlugin) {

    private var townyAvailable = false
    private var getTownOrNull: java.lang.reflect.Method? = null
    private var townHasNation: java.lang.reflect.Method? = null
    private var nationIsAtWar: java.lang.reflect.Method? = null
    private var getResidentOrNull: java.lang.reflect.Method? = null
    private var residentHasTown: java.lang.reflect.Method? = null
    private var getTownBlock: java.lang.reflect.Method? = null
    private var townBlockHasTown: java.lang.reflect.Method? = null
    private var townIsPeaceful: java.lang.reflect.Method? = null

    fun init() {
        townyAvailable = plugin.server.pluginManager.getPlugin("Towny") != null
        if (townyAvailable) {
            try {
                initReflectionCache()
                plugin.logger.info("[TownyIntegration] Towny detected - enabling city/war checks")
            } catch (e: Exception) {
                plugin.logger.warning("[TownyIntegration] Towny found but reflection init failed: ${e.message}")
                townyAvailable = false
            }
        } else {
            plugin.logger.info("[TownyIntegration] Towny not found - city checks disabled")
        }
    }

    private fun initReflectionCache() {
        val townyUniverseClass = Class.forName("com.palmergames.bukkit.towny.TownyUniverse")
        val getInstanceMethod = townyUniverseClass.getMethod("getInstance")
        val universe = getInstanceMethod.invoke(null)

        getResidentOrNull = townyUniverseClass.getMethod("getResident", java.util.UUID::class.java)

        val residentClass = Class.forName("com.palmergames.bukkit.towny.object.Resident")
        residentHasTown = residentClass.getMethod("hasTown")

        val townClass = Class.forName("com.palmergames.bukkit.towny.object.Town")
        getTownOrNull = residentClass.getMethod("getTownOrNull")
        townHasNation = townClass.getMethod("hasNation")
        townIsPeaceful = townClass.getMethod("isNeutral")

        val nationClass = Class.forName("com.palmergames.bukkit.towny.object.Nation")
        nationIsAtWar = nationClass.getMethod("isAtWar")

        val townyAPI = Class.forName("com.palmergames.bukkit.towny.TownyAPI")
        val getTownyAPIMethod = townyAPI.getMethod("getInstance")
        val api = getTownyAPIMethod.invoke(null)

        getTownBlock = townyAPI.getMethod("getTownBlock", org.bukkit.Location::class.java)

        val townBlockClass = Class.forName("com.palmergames.bukkit.towny.object.TownBlock")
        townBlockHasTown = townBlockClass.getMethod("hasTown")
    }

    fun isPlayerInCity(player: Player): Boolean {
        if (!townyAvailable) return false
        return try {
            val api = Class.forName("com.palmergames.bukkit.towny.TownyAPI").getMethod("getInstance").invoke(null)
            val townBlock = getTownBlock?.invoke(api, player.location) ?: return false
            val hasTown = townBlockHasTown?.invoke(townBlock) as? Boolean ?: false
            hasTown
        } catch (e: Exception) {
            false
        }
    }

    fun isNationAtWar(player: Player): Boolean {
        if (!townyAvailable) return false
        return try {
            val universe = Class.forName("com.palmergames.bukkit.towny.TownyUniverse").getMethod("getInstance").invoke(null)
            val resident = getResidentOrNull?.invoke(universe, player.uniqueId) ?: return false
            val hasTown = residentHasTown?.invoke(resident) as? Boolean ?: return false
            if (!hasTown) return false

            val town = getTownOrNull?.invoke(resident) ?: return false
            val hasNation = townHasNation?.invoke(town) as? Boolean ?: false
            if (!hasNation) return false

            val nation = town.javaClass.getMethod("getNationOrNull").invoke(town) ?: return false
            nationIsAtWar?.invoke(nation) as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun isTownPeaceful(player: Player): Boolean {
        if (!townyAvailable) return false
        return try {
            val universe = Class.forName("com.palmergames.bukkit.towny.TownyUniverse").getMethod("getInstance").invoke(null)
            val resident = getResidentOrNull?.invoke(universe, player.uniqueId) ?: return false
            val hasTown = residentHasTown?.invoke(resident) as? Boolean ?: false
            if (!hasTown) return false

            val town = getTownOrNull?.invoke(resident) ?: return false
            townIsPeaceful?.invoke(town) as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun canUseCombatAbilities(player: Player): Boolean {
        if (!townyAvailable) return true

        val inCity = isPlayerInCity(player)
        if (!inCity) return true

        val atWar = isNationAtWar(player)
        return atWar
    }

    fun isAvailable(): Boolean = townyAvailable
}
