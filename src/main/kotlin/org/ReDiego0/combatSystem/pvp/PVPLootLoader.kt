package org.ReDiego0.combatSystem.pvp

import org.ReDiego0.combatSystem.data.LootPool
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PVPLootLoader(private val plugin: JavaPlugin) {

    private val pvpPools = mutableMapOf<String, LootPool>()
    private val pvpLootDir = File(plugin.dataFolder, "pvp-loot")

    fun loadAll() {
        pvpLootDir.mkdirs()
        pvpPools.clear()

        val examplesDir = File(pvpLootDir, "_examples")
        if (!examplesDir.exists()) {
            generateExamples(examplesDir)
        }

        for (file in scanYamlFiles(pvpLootDir)) {
            if (file.parentFile?.name == "_examples") continue
            loadPool(file)
        }

        plugin.logger.info("[PVPLootLoader] Loaded ${pvpPools.size} PVP loot pools")
    }

    private fun loadPool(file: File) {
        try {
            val yaml = YamlConfiguration.loadConfiguration(file)

            val id = yaml.getString("id") ?: run {
                plugin.logger.warning("[PVPLootLoader] Missing 'id' in ${file.name}")
                return
            }
            val displayName = yaml.getString("display-name") ?: id
            val description = yaml.getString("description") ?: ""

            val conditionsSection = yaml.getConfigurationSection("conditions")
            val conditions = loadConditions(conditionsSection)

            val dropsSection = yaml.getConfigurationSection("drops")
            val drops = loadDrops(dropsSection)

            pvpPools[id] = LootPool(
                id = id,
                displayName = displayName,
                description = description,
                conditions = conditions,
                drops = drops
            )

            if (plugin.config.getBoolean("debug", false)) {
                plugin.logger.info("[PVPLootLoader] Loaded PVP pool: $id (${drops.size} drops)")
            }
        } catch (e: Exception) {
            plugin.logger.warning("[PVPLootLoader] Failed to load ${file.name}: ${e.message}")
        }
    }

    private fun loadConditions(section: org.bukkit.configuration.ConfigurationSection?): LootPool.LootConditions {
        if (section == null) return emptyConditions()

        val timeSection = section.getConfigurationSection("time-of-day")
        val timeRange = if (timeSection != null) {
            LootPool.LootConditions.TimeRange(
                min = timeSection.getInt("min", 0),
                max = timeSection.getInt("max", 24000)
            )
        } else null

        return LootPool.LootConditions(
            worlds = section.getStringList("worlds"),
            mobs = section.getStringList("mobs"),
            minEnemyLevel = if (section.contains("min-enemy-level")) section.getInt("min-enemy-level") else null,
            maxEnemyLevel = if (section.contains("max-enemy-level")) section.getInt("max-enemy-level") else null,
            biomes = section.getStringList("biomes"),
            timeOfDay = timeRange,
            requiredPermission = section.getString("required-permission"),
            minPlayerLevel = if (section.contains("min-player-level")) section.getInt("min-player-level") else null,
            maxPlayerLevel = if (section.contains("max-player-level")) section.getInt("max-player-level") else null,
            requiredWorldGuardRegion = section.getString("required-worldguard-region"),
            chance = if (section.contains("chance")) section.getDouble("chance") else null
        )
    }

    private fun emptyConditions(): LootPool.LootConditions {
        return LootPool.LootConditions(
            worlds = emptyList(),
            mobs = emptyList(),
            minEnemyLevel = null,
            maxEnemyLevel = null,
            biomes = emptyList(),
            timeOfDay = null,
            requiredPermission = null,
            minPlayerLevel = null,
            maxPlayerLevel = null,
            requiredWorldGuardRegion = null,
            chance = null
        )
    }

    private fun loadDrops(section: org.bukkit.configuration.ConfigurationSection?): List<LootPool.LootDrop> {
        if (section == null) return emptyList()
        val drops = mutableListOf<LootPool.LootDrop>()

        for (key in section.getKeys(false)) {
            val dropSection = section.getConfigurationSection(key) ?: continue
            drops.add(
                LootPool.LootDrop(
                    weaponId = dropSection.getString("weapon-id") ?: dropSection.getString("item-id") ?: continue,
                    weight = dropSection.getInt("weight", 100)
                )
            )
        }

        return drops
    }

    private fun generateExamples(dir: File) {
        dir.mkdirs()

        val file = File(dir, "_example_pvp_drops.yml")
        if (file.exists()) return

        val yaml = YamlConfiguration()

        yaml.set("id", "pvp_general_drops")
        yaml.set("display-name", "PVP General Drops")
        yaml.set("description", "Drops from PVP kills")

        yaml.set("conditions.worlds", listOf("world", "pvp_arena_"))
        yaml.set("conditions.chance", 0.5)

        yaml.set("drops.pvp_sword.weapon-id", "pvp_exclusive_sword")
        yaml.set("drops.pvp_sword.weight", 50)
        yaml.set("drops.pvp_heal.item-id", "healing_potion_v2")
        yaml.set("drops.pvp_heal.weight", 200)

        val header = buildString {
            appendLine("CombatSystem - PVP Loot Pool Example")
            appendLine("Rename and move out of _examples/ to register.")
        }
        yaml.options().header(header)
        yaml.save(file)

        plugin.logger.info("[PVPLootLoader] Generated example PVP loot pool in ${dir.path}")
    }

    private fun scanYamlFiles(dir: File): List<File> {
        val result = mutableListOf<File>()
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                result.addAll(scanYamlFiles(file))
            } else if (file.extension == "yml") {
                result.add(file)
            }
        }
        return result
    }

    fun getPool(id: String): LootPool? = pvpPools[id]

    fun getAllPools(): Map<String, LootPool> = pvpPools.toMap()
}
