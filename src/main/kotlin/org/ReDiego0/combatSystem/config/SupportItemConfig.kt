package org.ReDiego0.combatSystem.config

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SupportItemConfig(private val plugin: JavaPlugin) {

    private lateinit var config: YamlConfiguration
    private val configFile = File(plugin.dataFolder, "support-items.yml")

    fun load() {
        plugin.dataFolder.mkdirs()
        if (!configFile.exists()) {
            generateDefault()
        }
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun reload() {
        load()
    }

    private fun generateDefault() {
        val yaml = YamlConfiguration()

        yaml.set("support-items.healing_potion.display-name", "§cHealing Potion")
        yaml.set("support-items.healing_potion.material", "POTION")
        yaml.set("support-items.healing_potion.slot", 0)
        yaml.set("support-items.healing_potion.effects.0.type", "INSTANT_HEALTH")
        yaml.set("support-items.healing_potion.effects.0.amount", 6)
        yaml.set("support-items.healing_potion.cooldown", 10.0)

        yaml.set("support-items.speed_potion.display-name", "§bSpeed Potion")
        yaml.set("support-items.speed_potion.material", "POTION")
        yaml.set("support-items.speed_potion.slot", 1)
        yaml.set("support-items.speed_potion.effects.0.type", "SPEED")
        yaml.set("support-items.speed_potion.effects.0.duration", 15)
        yaml.set("support-items.speed_potion.effects.0.level", 2)
        yaml.set("support-items.speed_potion.cooldown", 20.0)

        yaml.set("support-items.smoke_bomb.display-name", "§7Smoke Bomb")
        yaml.set("support-items.smoke_bomb.material", "GRAY_DYE")
        yaml.set("support-items.smoke_bomb.slot", 2)
        yaml.set("support-items.smoke_bomb.effects.0.type", "AREA_SMOKE")
        yaml.set("support-items.smoke_bomb.effects.0.radius", 5.0)
        yaml.set("support-items.smoke_bomb.effects.0.duration", 8.0)
        yaml.set("support-items.smoke_bomb.cooldown", 30.0)

        yaml.set("support-items.paralyzing_dagger.display-name", "§dParalyzing Dagger")
        yaml.set("support-items.paralyzing_dagger.material", "IRON_SWORD")
        yaml.set("support-items.paralyzing_dagger.slot", 3)
        yaml.set("support-items.paralyzing_dagger.effects.0.type", "STUN_ON_HIT")
        yaml.set("support-items.paralyzing_dagger.effects.0.chance", 1.0)
        yaml.set("support-items.paralyzing_dagger.effects.0.duration", 3.0)
        yaml.set("support-items.paralyzing_dagger.cooldown", 25.0)

        yaml.set("support-items.fire_grenade.display-name", "§6Fire Grenade")
        yaml.set("support-items.fire_grenade.material", "FIRE_CHARGE")
        yaml.set("support-items.fire_grenade.slot", 4)
        yaml.set("support-items.fire_grenade.effects.0.type", "AOE_DAMAGE")
        yaml.set("support-items.fire_grenade.effects.0.radius", 4.0)
        yaml.set("support-items.fire_grenade.effects.0.damage", 8.0)
        yaml.set("support-items.fire_grenade.effects.0.fire-ticks", 60)
        yaml.set("support-items.fire_grenade.cooldown", 30.0)

        yaml.set("support-items.shield_totem.display-name", "§eShield Totem")
        yaml.set("support-items.shield_totem.material", "TOTEM_OF_UNDYING")
        yaml.set("support-items.shield_totem.slot", 5)
        yaml.set("support-items.shield_totem.effects.0.type", "DAMAGE_REDUCTION")
        yaml.set("support-items.shield_totem.effects.0.radius", 5.0)
        yaml.set("support-items.shield_totem.effects.0.reduction", 0.3)
        yaml.set("support-items.shield_totem.effects.0.duration", 10.0)
        yaml.set("support-items.shield_totem.cooldown", 45.0)

        yaml.save(configFile)
        plugin.logger.info("[SupportItemConfig] Default support-items.yml generated")
    }

    fun getSupportItems(): Map<String, SupportItemData> {
        val items = mutableMapOf<String, SupportItemData>()

        val section = config.getConfigurationSection("support-items") ?: return items

        for (key in section.getKeys(false)) {
            val itemSection = section.getConfigurationSection(key) ?: continue
            val displayName = itemSection.getString("display-name") ?: key
            val materialName = itemSection.getString("material") ?: "STONE"
            val material = Material.matchMaterial(materialName) ?: Material.STONE
            val slot = itemSection.getInt("slot", 0)
            val cooldown = itemSection.getDouble("cooldown", 10.0)

            val effects = mutableListOf<Map<String, Any>>()
            val effectsSection = itemSection.getConfigurationSection("effects")
            if (effectsSection != null) {
                for (effectKey in effectsSection.getKeys(false)) {
                    val effectSection = effectsSection.getConfigurationSection(effectKey) ?: continue
                    val effectMap = mutableMapOf<String, Any>()
                    for (paramKey in effectSection.getKeys(false)) {
                        val value = effectSection.get(paramKey)
                        if (value != null) {
                            effectMap[paramKey] = value
                        }
                    }
                    effects.add(effectMap)
                }
            }

            items[key] = SupportItemData(key, displayName, material, slot, cooldown, effects)
        }

        return items
    }

    data class SupportItemData(
        val id: String,
        val displayName: String,
        val material: Material,
        val slot: Int,
        val cooldown: Double,
        val effects: List<Map<String, Any>>
    ) {
        fun toItemStack(): ItemStack {
            val item = ItemStack(material)
            val meta = item.itemMeta
            meta?.setDisplayName(displayName)
            item.itemMeta = meta
            return item
        }
    }
}
