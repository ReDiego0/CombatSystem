package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.data.ArmorBonus
import org.ReDiego0.combatSystem.data.PassiveTrigger
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class ArmorBonusLoader(private val plugin: JavaPlugin) {

    private val bonuses = mutableMapOf<String, ArmorBonus>()
    private val armorDir = File(plugin.dataFolder, "armor")

    fun loadAll() {
        armorDir.mkdirs()
        bonuses.clear()

        val bonusFile = File(armorDir, "bonuses.yml")
        if (!bonusFile.exists()) {
            generateExampleBonuses(bonusFile)
        }

        loadBonusesFromFile(bonusFile)

        for (file in scanYamlFiles(armorDir)) {
            if (file.name == "bonuses.yml") continue
            if (file.parentFile?.name == "_examples") continue

            val yaml = YamlConfiguration.loadConfiguration(file)
            val bonusesSection = yaml.getConfigurationSection("bonuses")
            if (bonusesSection != null) {
                loadBonusesFromSection(bonusesSection)
            }
        }

        plugin.logger.info("[ArmorBonusLoader] Loaded ${bonuses.size} armor bonuses")
    }

    private fun loadBonusesFromFile(file: File) {
        val yaml = YamlConfiguration.loadConfiguration(file)
        val bonusesSection = yaml.getConfigurationSection("bonuses") ?: return
        loadBonusesFromSection(bonusesSection)
    }

    private fun loadBonusesFromSection(section: org.bukkit.configuration.ConfigurationSection) {
        for (key in section.getKeys(false)) {
            val bonusSection = section.getConfigurationSection(key) ?: continue
            val bonus = parseBonus(key, bonusSection)
            if (bonus != null) {
                bonuses[key] = bonus
            }
        }
    }

    private fun parseBonus(id: String, section: org.bukkit.configuration.ConfigurationSection): ArmorBonus? {
        val displayName = section.getString("display-name") ?: id
        val description = section.getString("description") ?: ""
        val typeName = section.getString("type") ?: "PASSIVE"
        val bonusType = try {
            ArmorBonus.BonusType.valueOf(typeName.uppercase())
        } catch (e: Exception) {
            ArmorBonus.BonusType.PASSIVE
        }

        val triggerName = section.getString("trigger") ?: return null
        val trigger = PassiveTrigger.fromString(triggerName) ?: run {
            plugin.logger.warning("[ArmorBonusLoader] Invalid trigger '$triggerName' in bonus '$id'")
            return null
        }

        val conditionSection = section.getConfigurationSection("condition")
        val condition = if (conditionSection != null) {
            val map = mutableMapOf<String, Any>()
            for (key in conditionSection.getKeys(false)) {
                val value = conditionSection.get(key)
                if (value != null) map[key] = value
            }
            map
        } else null

        val effectSection = section.getConfigurationSection("effect")
        val effect = if (effectSection != null) {
            val map = mutableMapOf<String, Any>()
            for (key in effectSection.getKeys(false)) {
                val value = effectSection.get(key)
                if (value != null) map[key] = value
            }
            map
        } else {
            plugin.logger.warning("[ArmorBonusLoader] Missing 'effect' section in bonus '$id'")
            return null
        }

        return ArmorBonus(id, displayName, description, bonusType, trigger, condition, effect)
    }

    private fun generateExampleBonuses(file: File) {
        val yaml = YamlConfiguration()

        yaml.set("bonuses.fire_attack_speed.display-name", "Flame Frenzy")
        yaml.set("bonuses.fire_attack_speed.description", "Taking fire damage increases attack speed by 15% for 4s")
        yaml.set("bonuses.fire_attack_speed.type", "PASSIVE")
        yaml.set("bonuses.fire_attack_speed.trigger", "FIRE_DAMAGE_TAKEN")
        yaml.set("bonuses.fire_attack_speed.effect.type", "ATTACK_SPEED_BOOST")
        yaml.set("bonuses.fire_attack_speed.effect.value", 0.15)
        yaml.set("bonuses.fire_attack_speed.effect.duration", 4.0)

        yaml.set("bonuses.low_hp_defense.display-name", "Last Stand")
        yaml.set("bonuses.low_hp_defense.description", "When below 50% HP, gain 20% damage reduction for 5s")
        yaml.set("bonuses.low_hp_defense.type", "CONDITIONAL")
        yaml.set("bonuses.low_hp_defense.trigger", "LOW_HP")
        yaml.set("bonuses.low_hp_defense.condition.hp-threshold", 0.5)
        yaml.set("bonuses.low_hp_defense.effect.type", "DAMAGE_REDUCTION")
        yaml.set("bonuses.low_hp_defense.effect.value", 0.20)
        yaml.set("bonuses.low_hp_defense.effect.duration", 5.0)

        yaml.set("bonuses.kill_speed.display-name", "Blood Rush")
        yaml.set("bonuses.kill_speed.description", "Killing an enemy grants 10% speed for 3s")
        yaml.set("bonuses.kill_speed.type", "PASSIVE")
        yaml.set("bonuses.kill_speed.trigger", "ENEMY_KILLED")
        yaml.set("bonuses.kill_speed.effect.type", "SPEED_BOOST")
        yaml.set("bonuses.kill_speed.effect.value", 0.10)
        yaml.set("bonuses.kill_speed.effect.duration", 3.0)

        yaml.set("bonuses.dash_lifesteal.display-name", "Vampiric Dash")
        yaml.set("bonuses.dash_lifesteal.description", "Dashing grants 5% lifesteal for 4s")
        yaml.set("bonuses.dash_lifesteal.type", "PASSIVE")
        yaml.set("bonuses.dash_lifesteal.trigger", "DASH_USED")
        yaml.set("bonuses.dash_lifesteal.effect.type", "LIFESTEAL_BOOST")
        yaml.set("bonuses.dash_lifesteal.effect.value", 0.05)
        yaml.set("bonuses.dash_lifesteal.effect.duration", 4.0)

        val header = buildString {
            appendLine("CombatSystem - Armor Bonus Definitions")
            appendLine("Define reusable bonuses here, then reference them by ID in armor YAML files.")
        }
        yaml.options().header(header)
        yaml.save(file)

        plugin.logger.info("[ArmorBonusLoader] Generated example bonuses in ${file.path}")
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

    fun get(id: String): ArmorBonus? = bonuses[id]

    fun getAll(): Map<String, ArmorBonus> = bonuses.toMap()
}
