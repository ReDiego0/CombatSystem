package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.data.LootPool
import org.ReDiego0.combatSystem.data.TraitPool
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PoolRegistry(private val plugin: JavaPlugin) {

    private val traitPools = mutableMapOf<String, TraitPool>()
    private val lootPools = mutableMapOf<String, LootPool>()
    private val traitsDir = File(plugin.dataFolder, "traits")
    private val lootPoolsDir = File(plugin.dataFolder, "loot-pools")

    fun loadAll() {
        traitsDir.mkdirs()
        lootPoolsDir.mkdirs()

        traitPools.clear()
        lootPools.clear()

        val traitsExampleDir = File(traitsDir, "_examples")
        if (!traitsExampleDir.exists()) {
            generateTraitExamples(traitsExampleDir)
        }

        val lootExampleDir = File(lootPoolsDir, "_examples")
        if (!lootExampleDir.exists()) {
            generateLootExamples(lootExampleDir)
        }

        for (file in scanYamlFiles(traitsDir)) {
            if (file.parentFile?.name == "_examples") continue
            loadTraitPool(file)
        }

        for (file in scanYamlFiles(lootPoolsDir)) {
            if (file.parentFile?.name == "_examples") continue
            loadLootPool(file)
        }

        plugin.logger.info("[PoolRegistry] Loaded ${traitPools.size} trait pools, ${lootPools.size} loot pools")
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

    private fun loadTraitPool(file: File) {
        try {
            val yaml = YamlConfiguration.loadConfiguration(file)
            val id = yaml.getString("id") ?: run {
                plugin.logger.warning("[PoolRegistry] Missing 'id' in trait pool ${file.name}")
                return
            }

            val traitsSection = yaml.getConfigurationSection("traits") ?: run {
                plugin.logger.warning("[PoolRegistry] Missing 'traits' section in ${file.name}")
                return
            }

            val traits = mutableMapOf<String, Int>()
            for (key in traitsSection.getKeys(false)) {
                traits[key] = traitsSection.getInt(key, 100)
            }

            traitPools[id] = TraitPool(id = id, traits = traits)

            if (plugin.config.getBoolean("debug", false)) {
                plugin.logger.info("[PoolRegistry] Loaded trait pool: $id (${traits.size} traits)")
            }
        } catch (e: Exception) {
            plugin.logger.warning("[PoolRegistry] Failed to load trait pool ${file.name}: ${e.message}")
        }
    }

    private fun loadLootPool(file: File) {
        try {
            val yaml = YamlConfiguration.loadConfiguration(file)
            val id = yaml.getString("id") ?: run {
                plugin.logger.warning("[PoolRegistry] Missing 'id' in loot pool ${file.name}")
                return
            }
            val displayName = yaml.getString("display-name") ?: id
            val description = yaml.getString("description") ?: ""

            val conditions = loadLootConditions(yaml.getConfigurationSection("conditions"))
            val drops = loadLootDrops(yaml.getConfigurationSection("drops"))

            lootPools[id] = LootPool(
                id = id,
                displayName = displayName,
                description = description,
                conditions = conditions,
                drops = drops
            )

            if (plugin.config.getBoolean("debug", false)) {
                plugin.logger.info("[PoolRegistry] Loaded loot pool: $id (${drops.size} drops)")
            }
        } catch (e: Exception) {
            plugin.logger.warning("[PoolRegistry] Failed to load loot pool ${file.name}: ${e.message}")
        }
    }

    private fun loadLootConditions(section: org.bukkit.configuration.ConfigurationSection?): LootPool.LootConditions {
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

    private fun loadLootDrops(section: org.bukkit.configuration.ConfigurationSection?): List<LootPool.LootDrop> {
        if (section == null) return emptyList()
        val drops = mutableListOf<LootPool.LootDrop>()

        for (key in section.getKeys(false)) {
            val dropSection = section.getConfigurationSection(key) ?: continue
            drops.add(
                LootPool.LootDrop(
                    weaponId = dropSection.getString("weapon-id") ?: continue,
                    weight = dropSection.getInt("weight", 100)
                )
            )
        }

        return drops
    }

    private fun generateTraitExamples(dir: File) {
        dir.mkdirs()
        val categories = listOf("katana", "longsword", "heavy_hammer", "crossbow", "spear", "staff", "bow")

        for (cat in categories) {
            val file = File(dir, "_example_pool_${cat}.yml")
            if (file.exists()) continue

            val yaml = YamlConfiguration()
            yaml.set("id", "${cat}_common")
            yaml.set("traits.rapid_strike", 100)
            yaml.set("traits.vampiric_edge", 80)
            yaml.set("traits.frost_nova", 60)
            yaml.set("traits.thunder_clap", 40)

            val header = buildString {
                appendLine("CombatSystem - Example Trait Pool")
                appendLine("Rename and move out of _examples/ to register.")
            }
            yaml.options().header(header)
            yaml.save(file)
        }

        plugin.logger.info("[PoolRegistry] Generated example trait pools in ${dir.path}")
    }

    private fun generateLootExamples(dir: File) {
        dir.mkdirs()

        val file = File(dir, "_example_loot_pool.yml")
        if (file.exists()) return

        val yaml = YamlConfiguration()
        yaml.set("id", "example_loot_pool")
        yaml.set("display-name", "Example Loot Pool")
        yaml.set("description", "Example loot pool configuration")

        yaml.set("conditions.worlds", listOf("world", "dungeon_"))
        yaml.set("conditions.mobs", listOf("ZOMBIE", "SKELETON"))
        yaml.set("conditions.min-enemy-level", 1)
        yaml.set("conditions.max-enemy-level", 40)
        yaml.set("conditions.biomes", listOf("PLAINS", "FOREST"))
        yaml.set("conditions.time-of-day.min", 0)
        yaml.set("conditions.time-of-day.max", 24000)
        yaml.set("conditions.chance", 0.1)

        yaml.set("drops.example_katana.weapon-id", "example_katana")
        yaml.set("drops.example_katana.weight", 100)
        yaml.set("drops.example_bow.weapon-id", "example_bow")
        yaml.set("drops.example_bow.weight", 50)

        val header = buildString {
            appendLine("CombatSystem - Example Loot Pool")
            appendLine("Rename and move out of _examples/ to register.")
        }
        yaml.options().header(header)
        yaml.save(file)

        plugin.logger.info("[PoolRegistry] Generated example loot pool in ${dir.path}")
    }

    fun getTraitPool(id: String): TraitPool? = traitPools[id]

    fun getLootPool(id: String): LootPool? = lootPools[id]

    fun getAllTraitPools(): Map<String, TraitPool> = traitPools.toMap()

    fun getAllLootPools(): Map<String, LootPool> = lootPools.toMap()
}
