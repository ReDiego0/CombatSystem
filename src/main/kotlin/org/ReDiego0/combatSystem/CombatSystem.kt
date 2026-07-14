package org.ReDiego0.combatSystem

import org.ReDiego0.combatSystem.config.ConfigManager
import org.ReDiego0.combatSystem.core.PDCUtil
import org.ReDiego0.combatSystem.item.PoolRegistry
import org.ReDiego0.combatSystem.item.WeaponRegistry
import org.ReDiego0.combatSystem.listener.WorldIsolationListener
import org.ReDiego0.combatSystem.world.SafeZoneManager
import org.ReDiego0.combatSystem.world.WorldIsolation
import org.bukkit.plugin.java.JavaPlugin

class CombatSystem : JavaPlugin() {

    companion object {
        lateinit var instance: CombatSystem
            private set
    }

    lateinit var configManager: ConfigManager
        private set
    lateinit var worldIsolation: WorldIsolation
        private set
    lateinit var worldIsolationListener: WorldIsolationListener
        private set
    lateinit var safeZoneManager: SafeZoneManager
        private set
    lateinit var weaponRegistry: WeaponRegistry
        private set
    lateinit var poolRegistry: PoolRegistry
        private set

    override fun onEnable() {
        instance = this

        logger.info("============================================")
        logger.info("  CombatSystem - Looter-Shooter Combat Engine")
        logger.info("  Version: ${pluginMeta.version}")
        logger.info("============================================")

        initializeConfig()
        initializePDC()
        initializeWorldIsolation()
        initializeSafeZones()
        initializeRegistries()
        registerCommands()

        logger.info("[CombatSystem] Plugin enabled successfully!")
        logger.info("[CombatSystem] World Isolation Mode: ${configManager.getIsolationMode()}")
        logger.info("[CombatSystem] Safe Zones: ${if (configManager.isSafeZonesEnabled()) "ENABLED" else "DISABLED"}")
    }

    override fun onDisable() {
        if (::safeZoneManager.isInitialized) {
            safeZoneManager.clearAll()
        }
        if (::worldIsolationListener.isInitialized) {
            worldIsolationListener.clearSafeZones()
        }
        logger.info("[CombatSystem] Plugin disabled successfully!")
    }

    private fun initializeConfig() {
        configManager = ConfigManager(this)
        configManager.load()
        if (configManager.isDebug()) {
            logger.info("[CombatSystem] Debug mode enabled")
        }
    }

    private fun initializePDC() {
        PDCUtil.init(this)
        logger.info("[CombatSystem] PDCUtil initialized")
    }

    private fun initializeWorldIsolation() {
        worldIsolation = WorldIsolation(configManager)
        worldIsolationListener = WorldIsolationListener(this, worldIsolation, configManager)
        worldIsolationListener.register()
        logger.info("[CombatSystem] World Isolation initialized")
    }

    private fun initializeSafeZones() {
        safeZoneManager = SafeZoneManager(this, configManager, worldIsolationListener)
        safeZoneManager.init()
        logger.info("[CombatSystem] Safe Zone Manager initialized")
    }

    private fun initializeRegistries() {
        weaponRegistry = WeaponRegistry(this)
        weaponRegistry.loadAll()

        poolRegistry = PoolRegistry(this)
        poolRegistry.loadAll()

        logger.info("[CombatSystem] Registries initialized")
    }

    private fun registerCommands() {
        getCommand("combatsystem")?.setExecutor { sender, command, label, args ->
            if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
                if (sender.hasPermission("combatsystem.admin")) {
                    reloadPlugin()
                    sender.sendMessage("§a[CombatSystem] Configuration reloaded successfully!")
                } else {
                    sender.sendMessage("§c[CombatSystem] You don't have permission to do this.")
                }
                return@setExecutor true
            }

            sender.sendMessage("§6[CombatSystem] §fVersion ${pluginMeta.version}")
            sender.sendMessage("§7Use §e/cs reload §7to reload configuration.")
            true
        }

        getCommand("combatsystem")?.setTabCompleter { sender, command, alias, args ->
            if (args.size == 1) {
                return@setTabCompleter listOf("reload").filter { it.startsWith(args[0], ignoreCase = true) }
            }
            emptyList()
        }
    }

    fun reloadPlugin() {
        configManager.reload()
        worldIsolation.reload()
        safeZoneManager.clearAll()
        weaponRegistry.loadAll()
        poolRegistry.loadAll()
        logger.info("[CombatSystem] Plugin reloaded")
    }
}
