package org.ReDiego0.combatSystem.world

import org.ReDiego0.combatSystem.config.ConfigManager
import org.ReDiego0.combatSystem.listener.WorldIsolationListener
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class SafeZoneManager(
    private val plugin: JavaPlugin,
    private val configManager: ConfigManager,
    private val worldIsolationListener: WorldIsolationListener
) {

    private var worldGuardAvailable = false
    private val safeZoneFlag = configManager.getSafeZoneFlag()

    private var wgGetInstance: java.lang.reflect.Method? = null
    private var wgGetPlatform: java.lang.reflect.Method? = null
    private var platformGetRegionContainer: java.lang.reflect.Method? = null
    private var regionContainerGetApplicableRegions: java.lang.reflect.Method? = null
    private var applicableRegionsGetRegions: java.lang.reflect.Method? = null
    private var regionGetFlag: java.lang.reflect.Method? = null
    private var stateFlagAllow: Any? = null
    private var weLocationConstructor: java.lang.reflect.Constructor<*>? = null
    private var bukkitAdapterAdaptWorld: java.lang.reflect.Method? = null
    private var flag: Any? = null

    fun init() {
        worldGuardAvailable = plugin.server.pluginManager.getPlugin("WorldGuard") != null
        if (worldGuardAvailable) {
            try {
                initReflectionCache()
                plugin.logger.info("[SafeZoneManager] WorldGuard detected - enabling region-based safe zones")
            } catch (e: Exception) {
                plugin.logger.warning("[SafeZoneManager] WorldGuard found but reflection init failed: ${e.message}")
                worldGuardAvailable = false
            }
        } else {
            plugin.logger.info("[SafeZoneManager] WorldGuard not found - safe zones disabled")
        }
    }

    private fun initReflectionCache() {
        val wgClass = Class.forName("com.sk89q.worldguard.WorldGuard")
        wgGetInstance = wgClass.getMethod("getInstance")
        val worldGuard = wgGetInstance!!.invoke(null)

        wgGetPlatform = wgClass.getMethod("getPlatform")
        val platform = wgGetPlatform!!.invoke(worldGuard)

        platformGetRegionContainer = platform.javaClass.getMethod("getRegionContainer")
        val regionContainer = platformGetRegionContainer!!.invoke(platform)

        val weLocationClass = Class.forName("com.sk89q.worldedit.util.Location")
        val weWorldClass = Class.forName("com.sk89q.worldedit.world.World")
        weLocationConstructor = weLocationClass.getConstructor(
            weWorldClass, Double::class.java, Double::class.java, Double::class.java
        )

        val bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter")
        bukkitAdapterAdaptWorld = bukkitAdapterClass.getMethod("adapt", org.bukkit.World::class.java)

        regionContainerGetApplicableRegions = regionContainer.javaClass.getMethod(
            "getApplicableRegions",
            weLocationClass
        )

        val applicableRegionSetClass = Class.forName("com.sk89q.worldguard.protection.regions.ApplicableRegionSet")
        applicableRegionsGetRegions = applicableRegionSetClass.getMethod("getRegions")

        val protectedRegionClass = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion")
        regionGetFlag = protectedRegionClass.getMethod(
            "getFlag",
            Class.forName("com.sk89q.worldguard.protection.flags.Flag")
        )

        val stateFlagClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag")
        val flagField = stateFlagClass.getField(safeZoneFlag)
        this.flag = flagField.get(null)

        val stateEnumClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag\$State")
        stateFlagAllow = stateEnumClass.getField("ALLOW").get(null)
    }

    fun isInSafeZone(player: Player): Boolean {
        if (!configManager.isSafeZonesEnabled() || !worldGuardAvailable) return false
        return checkWorldGuardFlag(player.location)
    }

    fun isInSafeZone(location: Location): Boolean {
        if (!configManager.isSafeZonesEnabled() || !worldGuardAvailable) return false
        return checkWorldGuardFlag(location)
    }

    fun updatePlayerSafeZoneStatus(player: Player) {
        if (!configManager.isSafeZonesEnabled()) return
        val currentlyInSafeZone = isInSafeZone(player)
        val wasInSafeZone = worldIsolationListener.isInSafeZone(player)
        if (currentlyInSafeZone && !wasInSafeZone) {
            worldIsolationListener.markPlayerInSafeZone(player)
            if (configManager.isDebug()) {
                plugin.logger.info("[SafeZoneManager] Player ${player.name} entered safe zone")
            }
        } else if (!currentlyInSafeZone && wasInSafeZone) {
            worldIsolationListener.markPlayerLeftSafeZone(player)
            if (configManager.isDebug()) {
                plugin.logger.info("[SafeZoneManager] Player ${player.name} left safe zone")
            }
        }
    }

    fun clearPlayer(player: Player) {
        worldIsolationListener.markPlayerLeftSafeZone(player)
    }

    fun clearAll() {
        worldIsolationListener.clearSafeZones()
    }

    private fun checkWorldGuardFlag(location: Location): Boolean {
        try {
            val worldGuard = wgGetInstance!!.invoke(null)
            val platform = wgGetPlatform!!.invoke(worldGuard)
            val regionContainer = platformGetRegionContainer!!.invoke(platform)

            val weWorld = bukkitAdapterAdaptWorld!!.invoke(null, location.world)
            val weLocation = weLocationConstructor!!.newInstance(
                weWorld, location.x, location.y, location.z
            )

            val applicableRegions = regionContainerGetApplicableRegions!!.invoke(regionContainer, weLocation)
            val regions = applicableRegionsGetRegions!!.invoke(applicableRegions) as Collection<*>

            for (region in regions) {
                val flagValue = regionGetFlag!!.invoke(region, flag)
                if (flagValue == stateFlagAllow) {
                    return true
                }
            }
        } catch (e: ClassNotFoundException) {
            if (configManager.isDebug()) {
                plugin.logger.warning("[SafeZoneManager] WorldGuard class not found: ${e.message}")
            }
        } catch (e: Exception) {
            plugin.logger.warning("[SafeZoneManager] Error checking WorldGuard flag: ${e.message}")
            if (configManager.isDebug()) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun isWorldGuardAvailable(): Boolean = worldGuardAvailable
}
