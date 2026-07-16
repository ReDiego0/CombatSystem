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

        yaml.set("support-items.healing_potion_v1.display-name", "§cHealing Potion §7(Lv.1)")
        yaml.set("support-items.healing_potion_v1.material", "POTION")
        yaml.set("support-items.healing_potion_v1.slot", 0)
        yaml.set("support-items.healing_potion_v1.cooldown", 15.0)
        yaml.set("support-items.healing_potion_v1.effects.0.type", "INSTANT_HEALTH")
        yaml.set("support-items.healing_potion_v1.effects.0.amount", 4)

        yaml.set("support-items.healing_potion_v2.display-name", "§cHealing Potion §a(Lv.2)")
        yaml.set("support-items.healing_potion_v2.material", "POTION")
        yaml.set("support-items.healing_potion_v2.slot", 0)
        yaml.set("support-items.healing_potion_v2.cooldown", 12.0)
        yaml.set("support-items.healing_potion_v2.effects.0.type", "INSTANT_HEALTH")
        yaml.set("support-items.healing_potion_v2.effects.0.amount", 8)

        yaml.set("support-items.healing_potion_v3.display-name", "§cHealing Potion §b(Lv.3)")
        yaml.set("support-items.healing_potion_v3.material", "POTION")
        yaml.set("support-items.healing_potion_v3.slot", 0)
        yaml.set("support-items.healing_potion_v3.cooldown", 10.0)
        yaml.set("support-items.healing_potion_v3.effects.0.type", "INSTANT_HEALTH")
        yaml.set("support-items.healing_potion_v3.effects.0.amount", 12)
        yaml.set("support-items.healing_potion_v3.effects.1.type", "REGENERATION")
        yaml.set("support-items.healing_potion_v3.effects.1.duration", 3.0)
        yaml.set("support-items.healing_potion_v3.effects.1.level", 1)

        yaml.set("support-items.speed_potion_v1.display-name", "§bSpeed Potion §7(Lv.1)")
        yaml.set("support-items.speed_potion_v1.material", "POTION")
        yaml.set("support-items.speed_potion_v1.slot", 1)
        yaml.set("support-items.speed_potion_v1.cooldown", 20.0)
        yaml.set("support-items.speed_potion_v1.effects.0.type", "SPEED")
        yaml.set("support-items.speed_potion_v1.effects.0.duration", 10)
        yaml.set("support-items.speed_potion_v1.effects.0.level", 1)

        yaml.set("support-items.speed_potion_v2.display-name", "§bSpeed Potion §a(Lv.2)")
        yaml.set("support-items.speed_potion_v2.material", "POTION")
        yaml.set("support-items.speed_potion_v2.slot", 1)
        yaml.set("support-items.speed_potion_v2.cooldown", 18.0)
        yaml.set("support-items.speed_potion_v2.effects.0.type", "SPEED")
        yaml.set("support-items.speed_potion_v2.effects.0.duration", 15)
        yaml.set("support-items.speed_potion_v2.effects.0.level", 2)

        yaml.set("support-items.smoke_bomb_v1.display-name", "§7Smoke Bomb §7(Lv.1)")
        yaml.set("support-items.smoke_bomb_v1.material", "GRAY_DYE")
        yaml.set("support-items.smoke_bomb_v1.slot", 2)
        yaml.set("support-items.smoke_bomb_v1.cooldown", 30.0)
        yaml.set("support-items.smoke_bomb_v1.effects.0.type", "AREA_SMOKE")
        yaml.set("support-items.smoke_bomb_v1.effects.0.radius", 4.0)
        yaml.set("support-items.smoke_bomb_v1.effects.0.duration", 6.0)

        yaml.set("support-items.smoke_bomb_v2.display-name", "§7Smoke Bomb §a(Lv.2)")
        yaml.set("support-items.smoke_bomb_v2.material", "GRAY_DYE")
        yaml.set("support-items.smoke_bomb_v2.slot", 2)
        yaml.set("support-items.smoke_bomb_v2.cooldown", 25.0)
        yaml.set("support-items.smoke_bomb_v2.effects.0.type", "AREA_SMOKE")
        yaml.set("support-items.smoke_bomb_v2.effects.0.radius", 6.0)
        yaml.set("support-items.smoke_bomb_v2.effects.0.duration", 10.0)

        yaml.set("support-items.fire_grenade_v1.display-name", "§6Fire Grenade §7(Lv.1)")
        yaml.set("support-items.fire_grenade_v1.material", "FIRE_CHARGE")
        yaml.set("support-items.fire_grenade_v1.slot", 3)
        yaml.set("support-items.fire_grenade_v1.cooldown", 30.0)
        yaml.set("support-items.fire_grenade_v1.effects.0.type", "AOE_DAMAGE")
        yaml.set("support-items.fire_grenade_v1.effects.0.radius", 3.0)
        yaml.set("support-items.fire_grenade_v1.effects.0.damage", 6.0)
        yaml.set("support-items.fire_grenade_v1.effects.0.fire-ticks", 40)

        yaml.set("support-items.fire_grenade_v2.display-name", "§6Fire Grenade §a(Lv.2)")
        yaml.set("support-items.fire_grenade_v2.material", "FIRE_CHARGE")
        yaml.set("support-items.fire_grenade_v2.slot", 3)
        yaml.set("support-items.fire_grenade_v2.cooldown", 25.0)
        yaml.set("support-items.fire_grenade_v2.effects.0.type", "AOE_DAMAGE")
        yaml.set("support-items.fire_grenade_v2.effects.0.radius", 5.0)
        yaml.set("support-items.fire_grenade_v2.effects.0.damage", 10.0)
        yaml.set("support-items.fire_grenade_v2.effects.0.fire-ticks", 60)

        yaml.set("support-items.shield_totem_v1.display-name", "§eShield Totem §7(Lv.1)")
        yaml.set("support-items.shield_totem_v1.material", "TOTEM_OF_UNDYING")
        yaml.set("support-items.shield_totem_v1.slot", 4)
        yaml.set("support-items.shield_totem_v1.cooldown", 45.0)
        yaml.set("support-items.shield_totem_v1.effects.0.type", "DAMAGE_REDUCTION")
        yaml.set("support-items.shield_totem_v1.effects.0.radius", 4.0)
        yaml.set("support-items.shield_totem_v1.effects.0.reduction", 0.2)
        yaml.set("support-items.shield_totem_v1.effects.0.duration", 8.0)

        yaml.set("support-items.shield_totem_v2.display-name", "§eShield Totem §a(Lv.2)")
        yaml.set("support-items.shield_totem_v2.material", "TOTEM_OF_UNDYING")
        yaml.set("support-items.shield_totem_v2.slot", 4)
        yaml.set("support-items.shield_totem_v2.cooldown", 40.0)
        yaml.set("support-items.shield_totem_v2.effects.0.type", "DAMAGE_REDUCTION")
        yaml.set("support-items.shield_totem_v2.effects.0.radius", 6.0)
        yaml.set("support-items.shield_totem_v2.effects.0.reduction", 0.3)
        yaml.set("support-items.shield_totem_v2.effects.0.duration", 12.0)

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
