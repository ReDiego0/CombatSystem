package org.ReDiego0.combatSystem.world

import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin

class RegionChecker(private val plugin: JavaPlugin) {

    private var worldGuardAvailable = false
    private var getApplicableRegions: java.lang.reflect.Method? = null
    private var getRegions: java.lang.reflect.Method? = null
    private var regionGetId: java.lang.reflect.Method? = null
    private var regionContainer: Any? = null

    fun init() {
        worldGuardAvailable = plugin.server.pluginManager.getPlugin("WorldGuard") != null
        if (worldGuardAvailable) {
            try {
                initReflectionCache()
                plugin.logger.info("[RegionChecker] WorldGuard detected - region checking enabled")
            } catch (e: Exception) {
                plugin.logger.warning("[RegionChecker] WorldGuard found but reflection failed: ${e.message}")
                worldGuardAvailable = false
            }
        } else {
            plugin.logger.info("[RegionChecker] WorldGuard not found - region checking disabled")
        }
    }

    private fun initReflectionCache() {
        val wgClass = Class.forName("com.sk89q.worldguard.WorldGuard")
        val getInstance = wgClass.getMethod("getInstance")
        val worldGuard = getInstance.invoke(null)

        val platform = wgClass.getMethod("getPlatform").invoke(worldGuard)
        regionContainer = platform.javaClass.getMethod("getRegionContainer").invoke(platform)

        val weLocationClass = Class.forName("com.sk89q.worldedit.util.Location")
        getApplicableRegions = regionContainer!!.javaClass.getMethod("getApplicableRegions", weLocationClass)

        val applicableRegionSetClass = Class.forName("com.sk89q.worldguard.protection.regions.ApplicableRegionSet")
        getRegions = applicableRegionSetClass.getMethod("getRegions")

        val protectedRegionClass = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion")
        regionGetId = protectedRegionClass.getMethod("getId")
    }

    fun getRegionAt(location: Location): String? {
        if (!worldGuardAvailable || regionContainer == null) return null

        try {
            val weLocationClass = Class.forName("com.sk89q.worldedit.util.Location")
            val weWorldClass = Class.forName("com.sk89q.worldedit.world.World")
            val bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter")
            val adaptWorld = bukkitAdapter.getMethod("adapt", org.bukkit.World::class.java)
            val weWorld = adaptWorld.invoke(null, location.world)

            val constructor = weLocationClass.getConstructor(
                weWorldClass, Double::class.java, Double::class.java, Double::class.java
            )
            val weLocation = constructor.newInstance(weWorld, location.x, location.y, location.z)

            val applicableRegions = getApplicableRegions!!.invoke(regionContainer, weLocation)
            val regions = getRegions!!.invoke(applicableRegions) as Collection<*>

            for (region in regions) {
                val id = regionGetId!!.invoke(region) as? String
                if (id != null) return id
            }
        } catch (e: Exception) {
            if (plugin.config.getBoolean("debug", false)) {
                plugin.logger.warning("[RegionChecker] Error checking region: ${e.message}")
            }
        }

        return null
    }

    fun isInRegion(location: Location, regionId: String): Boolean {
        val region = getRegionAt(location) ?: return false
        return region.contains(regionId, ignoreCase = true)
    }

    fun isAvailable(): Boolean = worldGuardAvailable
}
