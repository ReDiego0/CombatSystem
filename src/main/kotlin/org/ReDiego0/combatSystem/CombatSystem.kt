package org.ReDiego0.combatSystem

import org.ReDiego0.combatSystem.combat.ActionbarHUD
import org.ReDiego0.combatSystem.combat.CombatManager
import org.ReDiego0.combatSystem.combat.TacticalDash
import org.ReDiego0.combatSystem.config.ConfigManager
import org.ReDiego0.combatSystem.config.PVPConfig
import org.ReDiego0.combatSystem.config.ProgressionConfig
import org.ReDiego0.combatSystem.config.SupportItemConfig
import org.ReDiego0.combatSystem.core.PDCUtil
import org.ReDiego0.combatSystem.core.StaminaManager
import org.ReDiego0.combatSystem.database.DatabaseManager
import org.ReDiego0.combatSystem.gui.*
import org.ReDiego0.combatSystem.item.*
import org.ReDiego0.combatSystem.listener.*
import org.ReDiego0.combatSystem.loadout.*
import org.ReDiego0.combatSystem.loot.*
import org.ReDiego0.combatSystem.pvp.*
import org.ReDiego0.combatSystem.anti.*
import org.ReDiego0.combatSystem.command.*
import org.ReDiego0.combatSystem.world.SafeZoneManager
import org.ReDiego0.combatSystem.world.TownyIntegration
import org.ReDiego0.combatSystem.world.WorldIsolation
import org.ReDiego0.combatSystem.world.RegionChecker
import org.bukkit.plugin.java.JavaPlugin

class CombatSystem : JavaPlugin() {

    companion object {
        lateinit var instance: CombatSystem
            private set
    }

    lateinit var configManager: ConfigManager
        private set
    lateinit var progressionConfig: ProgressionConfig
        private set
    lateinit var supportItemConfig: SupportItemConfig
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
    lateinit var itemProgression: ItemProgression
        private set
    lateinit var traitLoader: TraitLoader
        private set
    lateinit var traitEngine: TraitEngine
        private set
    lateinit var armoryListener: ArmoryListener
        private set
    lateinit var staminaManager: StaminaManager
        private set
    lateinit var combatManager: CombatManager
        private set
    lateinit var actionbarHUD: ActionbarHUD
        private set
    lateinit var townyIntegration: TownyIntegration
        private set
    lateinit var tacticalDash: TacticalDash
        private set
    lateinit var databaseManager: DatabaseManager
        private set
    lateinit var loadoutManager: LoadoutManager
        private set
    lateinit var loadoutEquipper: LoadoutEquipper
        private set
    lateinit var armorBonusLoader: ArmorBonusLoader
        private set
    lateinit var armorLoader: ArmorLoader
        private set
    lateinit var armorRegistry: ArmorRegistry
        private set
    lateinit var armorPassiveManager: ArmorPassiveManager
        private set
    lateinit var regionChecker: RegionChecker
        private set
    lateinit var lootEngine: LootEngine
        private set
    lateinit var antiExploitManager: AntiExploitManager
        private set
    lateinit var pvpManager: PVPManager
        private set
    lateinit var pvpLootLoader: PVPLootLoader
        private set

    override fun onEnable() {
        instance = this

        logger.info("============================================")
        logger.info("  CombatSystem - Looter-Shooter Combat Engine")
        logger.info("  Version: ${pluginMeta.version}")
        logger.info("============================================")

        initializeConfig()
        initializePDC()
        initializeDatabase()
        initializeWorldIsolation()
        initializeSafeZones()
        initializeTowny()
        initializeRegistries()
        initializeProgression()
        initializeTraits()
        initializeCombat()
        initializeDash()
        initializeLoadout()
        initializeArmor()
        initializeLoot()
        initializeAntiExploit()
        initializePVP()
        registerCommands()

        logger.info("[CombatSystem] Plugin enabled successfully!")
        logger.info("[CombatSystem] World Isolation Mode: ${configManager.getIsolationMode()}")
        logger.info("[CombatSystem] Safe Zones: ${if (configManager.isSafeZonesEnabled()) "ENABLED" else "DISABLED"}")
        logger.info("[CombatSystem] Towny: ${if (townyIntegration.isAvailable()) "ENABLED" else "DISABLED"}")
    }

    override fun onDisable() {
        if (::safeZoneManager.isInitialized) {
            safeZoneManager.clearAll()
        }
        if (::worldIsolationListener.isInitialized) {
            worldIsolationListener.clearSafeZones()
        }
        if (::databaseManager.isInitialized) {
            databaseManager.close()
        }
        logger.info("[CombatSystem] Plugin disabled successfully!")
    }

    private fun initializeConfig() {
        configManager = ConfigManager(this)
        configManager.load()

        progressionConfig = ProgressionConfig(this)
        progressionConfig.load()

        supportItemConfig = SupportItemConfig(this)
        supportItemConfig.load()

        if (configManager.isDebug()) {
            logger.info("[CombatSystem] Debug mode enabled")
        }
    }

    private fun initializePDC() {
        PDCUtil.init(this)
        logger.info("[CombatSystem] PDCUtil initialized")
    }

    private fun initializeDatabase() {
        databaseManager = DatabaseManager(this)
        databaseManager.init()
        logger.info("[CombatSystem] Database initialized")
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

    private fun initializeTowny() {
        townyIntegration = TownyIntegration(this)
        townyIntegration.init()
        logger.info("[CombatSystem] Towny Integration initialized")
    }

    private fun initializeRegistries() {
        weaponRegistry = WeaponRegistry(this)
        weaponRegistry.loadAll()

        poolRegistry = PoolRegistry(this)
        poolRegistry.loadAll()

        logger.info("[CombatSystem] Registries initialized")
    }

    private fun initializeProgression() {
        itemProgression = ItemProgression(progressionConfig)

        val progressionListener = ItemProgressionListener(this, worldIsolation, configManager, itemProgression)
        progressionListener.register()

        logger.info("[CombatSystem] Item Progression initialized")
    }

    private fun initializeTraits() {
        traitLoader = TraitLoader(this)
        traitLoader.loadAll()

        traitEngine = TraitEngine(traitLoader, progressionConfig)

        armoryListener = ArmoryListener(this, traitEngine, traitLoader)
        armoryListener.register()

        val armoryCommand = ArmoryCommandListener(this, armoryListener)
        armoryCommand.register()

        logger.info("[CombatSystem] Trait Engine initialized")
    }

    private fun initializeCombat() {
        staminaManager = StaminaManager()
        combatManager = CombatManager()
        actionbarHUD = ActionbarHUD(staminaManager, combatManager)

        val combatInputListener = CombatInputListener(this, worldIsolation, configManager, staminaManager, combatManager, actionbarHUD, townyIntegration)
        combatInputListener.register()

        server.scheduler.runTaskTimer(this, Runnable {
            for (player in server.onlinePlayers) {
                staminaManager.regenerateStamina(player)
                actionbarHUD.update(player)
            }
        }, 20L, 20L)

        logger.info("[CombatSystem] Combat System initialized")
    }

    private fun initializeDash() {
        tacticalDash = TacticalDash(worldIsolation, townyIntegration, staminaManager)

        val dashInputListener = DashInputListener(this, tacticalDash)
        dashInputListener.register()

        logger.info("[CombatSystem] Tactical Dash initialized")
    }

    private fun initializeLoadout() {
        val loadoutStorage = LoadoutStorage(this, databaseManager)
        val resourceStorage = ResourceStorage(this, databaseManager)

        loadoutManager = LoadoutManager(loadoutStorage, resourceStorage)
        loadoutEquipper = LoadoutEquipper(this, loadoutManager, townyIntegration)

        val loadoutGUI = LoadoutGUI(loadoutManager)
        val loadoutListener = LoadoutListener(this, loadoutManager, loadoutGUI)
        loadoutListener.register()

        val supportBagGUI = SupportBagGUI(loadoutManager)
        val supportBagListener = SupportBagListener(this, loadoutManager, supportBagGUI)
        supportBagListener.register()

        val loadoutTownyListener = LoadoutTownyListener(this, townyIntegration, loadoutEquipper, loadoutManager)
        loadoutTownyListener.register()

        val loadoutDeathListener = LoadoutDeathListener(this, loadoutEquipper, loadoutManager, townyIntegration)
        loadoutDeathListener.register()

        val loadoutDisconnectListener = LoadoutDisconnectListener(this, loadoutEquipper, loadoutManager)
        loadoutDisconnectListener.register()

        getCommand("loadout")?.setExecutor { sender, command, label, args ->
            if (sender !is org.bukkit.entity.Player) {
                sender.sendMessage("§c[Loadout] This command can only be used by players.")
                return@setExecutor true
            }

            if (!sender.hasPermission("combatsystem.loadout")) {
                sender.sendMessage("§c[Loadout] You don't have permission to use this command.")
                return@setExecutor true
            }

            loadoutGUI.open(sender)
            true
        }

        logger.info("[CombatSystem] Loadout System initialized")
    }

    private fun initializeArmor() {
        armorBonusLoader = ArmorBonusLoader(this)
        armorBonusLoader.loadAll()

        armorLoader = ArmorLoader(this)
        armorLoader.loadAll()

        armorRegistry = ArmorRegistry(armorLoader, armorBonusLoader)

        val setBonusCalculator = SetBonusCalculator()
        armorPassiveManager = ArmorPassiveManager(armorRegistry, armorBonusLoader, setBonusCalculator)

        val armorPassiveListener = ArmorPassiveListener(this, armorRegistry, armorPassiveManager)
        armorPassiveListener.register()

        logger.info("[CombatSystem] Armor System initialized")
    }

    private fun initializeLoot() {
        regionChecker = RegionChecker(this)
        regionChecker.init()

        val mobLevelResolver = MobLevelResolver(this, regionChecker)
        val lootRoller = LootRoller()
        val lostLootStash = LostLootStash(this, databaseManager)
        lostLootStash.init()

        val lootDelivery = LootDelivery(this, loadoutManager, weaponRegistry, armorLoader, supportItemConfig, lostLootStash)
        val lootNotification = LootNotification()

        lootEngine = LootEngine(this, poolRegistry, lootRoller, lootDelivery, lootNotification, mobLevelResolver, regionChecker, worldIsolation)

        val lootMobKillListener = LootMobKillListener(this, lootEngine)
        lootMobKillListener.register()

        logger.info("[CombatSystem] Loot System initialized")
    }

    private fun initializeAntiExploit() {
        val spawnTracker = SpawnTracker()
        val bossDamageTracker = BossDamageTracker()
        val downscalingManager = DownscalingManager(this)

        antiExploitManager = AntiExploitManager(this, spawnTracker, bossDamageTracker, downscalingManager)
        antiExploitManager.init()

        val exploitListener = ExploitListener(this, antiExploitManager, downscalingManager)
        exploitListener.register()

        logger.info("[CombatSystem] Anti-Exploit System initialized")
    }

    private fun initializePVP() {
        val pvpConfig = PVPConfig(this)
        pvpConfig.load()

        val pvpXPCalculator = PVPXPCalculator(this)
        pvpLootLoader = PVPLootLoader(this)
        pvpLootLoader.loadAll()

        val lootRoller = LootRoller()
        val lostLootStash = LostLootStash(this, databaseManager)
        val lootDelivery = LootDelivery(this, loadoutManager, weaponRegistry, armorLoader, supportItemConfig, lostLootStash)
        val lootNotification = LootNotification()

        pvpManager = PVPManager(this, pvpXPCalculator, pvpLootLoader, lootRoller, lootDelivery, lootNotification, worldIsolation, townyIntegration)
        pvpManager.loadConfig(pvpConfig.config)

        val pvpKillListener = PVPKillListener(this, pvpManager)
        pvpKillListener.register()

        logger.info("[CombatSystem] PVP System initialized")
    }

    private fun registerCommands() {
        val adminCommand = AdminCommand(this, weaponRegistry, armorLoader, loadoutManager)
        adminCommand.register()
    }

    fun reloadPlugin() {
        configManager.reload()
        progressionConfig.reload()
        supportItemConfig.reload()
        worldIsolation.reload()
        safeZoneManager.clearAll()
        weaponRegistry.loadAll()
        poolRegistry.loadAll()
        traitLoader.loadAll()
        armorBonusLoader.loadAll()
        armorLoader.loadAll()
        pvpLootLoader.loadAll()
        logger.info("[CombatSystem] Plugin reloaded")
    }
}
