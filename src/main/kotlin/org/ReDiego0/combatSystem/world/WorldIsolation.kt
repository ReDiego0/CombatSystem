package org.ReDiego0.combatSystem.world

import org.ReDiego0.combatSystem.config.ConfigManager
import java.util.concurrent.ConcurrentHashMap

class WorldIsolation(private val configManager: ConfigManager) {

    private val worldCache = ConcurrentHashMap<String, Boolean>()
    private var mode: ConfigManager.IsolationMode = configManager.getIsolationMode()
    private var worldList: List<String> = configManager.getWorldList()

    fun reload() {
        mode = configManager.getIsolationMode()
        worldList = configManager.getWorldList()
        worldCache.clear()
        if (configManager.isDebug()) {
            println("[WorldIsolation] Reloaded - Mode: $mode, Worlds: $worldList")
        }
    }

    fun isWorldEnabled(worldName: String): Boolean {
        worldCache[worldName]?.let { return it }
        val result = calculateWorldEnabled(worldName)
        worldCache[worldName] = result
        if (configManager.isDebug()) {
            println("[WorldIsolation] World '$worldName' enabled: $result")
        }
        return result
    }

    private fun calculateWorldEnabled(worldName: String): Boolean {
        val matchesList = worldList.any { pattern ->
            worldName.contains(pattern, ignoreCase = true)
        }
        return when (mode) {
            ConfigManager.IsolationMode.WHITELIST -> matchesList
            ConfigManager.IsolationMode.BLACKLIST -> !matchesList
        }
    }

    fun clearCache() {
        worldCache.clear()
    }

    fun setCacheEntry(worldName: String, enabled: Boolean) {
        worldCache[worldName] = enabled
    }

    fun getMode(): ConfigManager.IsolationMode = mode

    fun getWorldList(): List<String> = worldList.toList()
}
