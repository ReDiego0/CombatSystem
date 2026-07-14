package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.data.Rarity
import org.ReDiego0.combatSystem.data.WeaponCategory
import org.ReDiego0.combatSystem.data.WeaponData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class WeaponRegistry(private val plugin: JavaPlugin) {

    private val weapons = mutableMapOf<String, WeaponData>()
    private val weaponsDir = File(plugin.dataFolder, "weapons")

    fun loadAll() {
        weaponsDir.mkdirs()
        weapons.clear()

        val exampleDir = File(weaponsDir, "_examples")
        if (!exampleDir.exists()) {
            generateExamples(exampleDir)
        }

        val files = scanYamlFiles(weaponsDir)
        for (file in files) {
            if (file.parentFile?.name == "_examples") continue
            loadWeapon(file)
        }

        plugin.logger.info("[WeaponRegistry] Loaded ${weapons.size} weapons")
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

    private fun loadWeapon(file: File) {
        try {
            val yaml = YamlConfiguration.loadConfiguration(file)

            val id = yaml.getString("id") ?: run {
                plugin.logger.warning("[WeaponRegistry] Missing 'id' in ${file.name}")
                return
            }
            val displayName = yaml.getString("display-name") ?: id
            val categoryName = yaml.getString("category") ?: run {
                plugin.logger.warning("[WeaponRegistry] Missing 'category' in ${file.name}")
                return
            }
            val rarityName = yaml.getString("rarity") ?: run {
                plugin.logger.warning("[WeaponRegistry] Missing 'rarity' in ${file.name}")
                return
            }

            val category = WeaponCategory.fromString(categoryName) ?: run {
                plugin.logger.warning("[WeaponRegistry] Invalid category '$categoryName' in ${file.name}")
                return
            }
            val rarity = Rarity.fromString(rarityName) ?: run {
                plugin.logger.warning("[WeaponRegistry] Invalid rarity '$rarityName' in ${file.name}")
                return
            }

            val tier = yaml.getInt("tier", 0)
            val baseDamage = yaml.getDouble("base-stats.damage", category.baseDamage)
            val attackSpeed = yaml.getDouble("base-stats.attack-speed", category.attackSpeed)
            val traitPool = yaml.getString("trait-pool")
            val lootPools = yaml.getStringList("loot-pools")

            if (rarity.canHaveTier() && tier < 1 || rarity.canHaveTier() && tier > rarity.maxTier) {
                plugin.logger.warning("[WeaponRegistry] Invalid tier $tier for rarity ${rarity.name} in ${file.name}")
            }

            val weaponData = WeaponData(
                id = id,
                displayName = displayName,
                category = category,
                rarity = rarity,
                tier = tier.coerceIn(0, rarity.maxTier),
                baseDamage = baseDamage,
                attackSpeed = attackSpeed,
                traitPool = traitPool,
                lootPools = lootPools
            )

            weapons[id] = weaponData

            if (plugin.config.getBoolean("debug", false)) {
                plugin.logger.info("[WeaponRegistry] Loaded weapon: $id (${rarity.name} ${category.name})")
            }
        } catch (e: Exception) {
            plugin.logger.warning("[WeaponRegistry] Failed to load ${file.name}: ${e.message}")
        }
    }

    private fun generateExamples(dir: File) {
        dir.mkdirs()

        for (category in WeaponCategory.entries) {
            val file = File(dir, "_example_${category.name.lowercase()}.yml")
            if (file.exists()) continue

            val rarity = Rarity.COMMON
            val yaml = YamlConfiguration()
            yaml.set("id", "example_${category.name.lowercase()}")
            yaml.set("display-name", "Example ${category.displayName}")
            yaml.set("category", category.name)
            yaml.set("rarity", rarity.name)
            yaml.set("tier", 0)
            yaml.set("base-stats.damage", category.baseDamage)
            yaml.set("base-stats.attack-speed", category.attackSpeed)
            yaml.set("trait-pool", "${category.name.lowercase()}_common")
            yaml.set("loot-pools", listOf("example_loot_pool"))

            val header = buildString {
                appendLine("CombatSystem - Example Weapon Configuration")
                appendLine("Category: ${category.name}")
                appendLine("Rename this file and move it out of _examples/ to register it.")
                appendLine("All .yml files inside weapons/ are loaded automatically.")
            }
            yaml.options().header(header)
            yaml.save(file)
        }

        plugin.logger.info("[WeaponRegistry] Generated example weapons in ${dir.path}")
    }

    fun get(id: String): WeaponData? = weapons[id]

    fun getAll(): Map<String, WeaponData> = weapons.toMap()

    fun getByCategory(category: WeaponCategory): List<WeaponData> {
        return weapons.values.filter { it.category == category }
    }

    fun getByRarity(rarity: Rarity): List<WeaponData> {
        return weapons.values.filter { it.rarity == rarity }
    }
}
