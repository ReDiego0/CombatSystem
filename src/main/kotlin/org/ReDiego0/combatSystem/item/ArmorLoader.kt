package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.data.ArmorData
import org.ReDiego0.combatSystem.data.Rarity
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class ArmorLoader(private val plugin: JavaPlugin) {

    private val armors = mutableMapOf<String, ArmorData>()
    private val armorDir = File(plugin.dataFolder, "armor")

    fun loadAll() {
        armorDir.mkdirs()
        armors.clear()

        val examplesDir = File(armorDir, "_examples")
        if (!examplesDir.exists()) {
            generateExamples(examplesDir)
        }

        for (file in scanYamlFiles(armorDir)) {
            if (file.name == "bonuses.yml") continue
            if (file.parentFile?.name == "_examples") continue
            loadArmorsFromFile(file)
        }

        plugin.logger.info("[ArmorLoader] Loaded ${armors.size} armor pieces")
    }

    private fun loadArmorsFromFile(file: File) {
        val yaml = YamlConfiguration.loadConfiguration(file)

        val armorsSection = yaml.getConfigurationSection("armors")
        if (armorsSection != null) {
            for (key in armorsSection.getKeys(false)) {
                val armorSection = armorsSection.getConfigurationSection(key) ?: continue
                val armor = parseArmor(key, armorSection)
                if (armor != null) {
                    armors[key] = armor
                }
            }
        } else {
            val armor = parseArmorFromRoot(yaml)
            if (armor != null) {
                armors[armor.id] = armor
            }
        }
    }

    private fun parseArmor(id: String, section: org.bukkit.configuration.ConfigurationSection): ArmorData? {
        val displayName = section.getString("display-name") ?: id
        val materialName = section.getString("material") ?: run {
            plugin.logger.warning("[ArmorLoader] Missing 'material' for armor '$id'")
            return null
        }
        val material = Material.matchMaterial(materialName) ?: run {
            plugin.logger.warning("[ArmorLoader] Invalid material '$materialName' for armor '$id'")
            return null
        }

        val setId = section.getString("set-id")
        val setPieceName = section.getString("set-piece")
        val setPiece = if (setPieceName != null) ArmorData.ArmorSlot.fromString(setPieceName) else null

        val rarityName = section.getString("rarity") ?: "COMMON"
        val rarity = Rarity.fromString(rarityName) ?: Rarity.COMMON
        val tier = section.getInt("tier", 0)

        val defense = section.getDouble("base-stats.defense", 0.0)
        val hpBonus = section.getDouble("base-stats.hp-bonus", 0.0)
        val bonusId = section.getString("bonus-id")

        return ArmorData(
            id = id,
            displayName = displayName,
            material = material,
            setId = setId,
            setPiece = setPiece,
            rarity = rarity,
            tier = tier.coerceIn(0, rarity.maxTier),
            defense = defense,
            hpBonus = hpBonus,
            bonusId = bonusId
        )
    }

    private fun parseArmorFromRoot(yaml: YamlConfiguration): ArmorData? {
        val id = yaml.getString("id") ?: return null
        return parseArmor(id, yaml)
    }

    private fun generateExamples(dir: File) {
        dir.mkdirs()

        val bonusFile = File(dir, "_example_armor.yml")
        if (bonusFile.exists()) return

        val yaml = YamlConfiguration()

        yaml.set("armors.dragon_knight_helmet.display-name", "Dragon Knight Helmet")
        yaml.set("armors.dragon_knight_helmet.material", "DIAMOND_HELMET")
        yaml.set("armors.dragon_knight_helmet.set-id", "dragon_knight")
        yaml.set("armors.dragon_knight_helmet.set-piece", "HELMET")
        yaml.set("armors.dragon_knight_helmet.rarity", "LEGENDARY")
        yaml.set("armors.dragon_knight_helmet.tier", 3)
        yaml.set("armors.dragon_knight_helmet.base-stats.defense", 10.0)
        yaml.set("armors.dragon_knight_helmet.base-stats.hp-bonus", 20.0)
        yaml.set("armors.dragon_knight_helmet.bonus-id", "fire_attack_speed")

        yaml.set("armors.dragon_knight_chest.display-name", "Dragon Knight Chestplate")
        yaml.set("armors.dragon_knight_chest.material", "DIAMOND_CHESTPLATE")
        yaml.set("armors.dragon_knight_chest.set-id", "dragon_knight")
        yaml.set("armors.dragon_knight_chest.set-piece", "CHESTPLATE")
        yaml.set("armors.dragon_knight_chest.rarity", "LEGENDARY")
        yaml.set("armors.dragon_knight_chest.tier", 3)
        yaml.set("armors.dragon_knight_chest.base-stats.defense", 15.0)
        yaml.set("armors.dragon_knight_chest.base-stats.hp-bonus", 40.0)
        yaml.set("armors.dragon_knight_chest.bonus-id", "low_hp_defense")

        val header = buildString {
            appendLine("CombatSystem - Example Armor Configuration")
            appendLine("Rename and move out of _examples/ to register.")
        }
        yaml.options().header(header)
        yaml.save(bonusFile)

        plugin.logger.info("[ArmorLoader] Generated example armor in ${dir.path}")
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

    fun get(id: String): ArmorData? = armors[id]

    fun getAll(): Map<String, ArmorData> = armors.toMap()

    fun getBySet(setId: String): List<ArmorData> {
        return armors.values.filter { it.setId == setId }
    }
}
