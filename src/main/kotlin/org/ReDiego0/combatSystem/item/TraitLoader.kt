package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.data.*
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TraitLoader(private val plugin: JavaPlugin) {

    private val traits = mutableMapOf<String, Trait>()
    private val traitsDir = File(plugin.dataFolder, "traits")

    fun loadAll() {
        traitsDir.mkdirs()
        traits.clear()

        val examplesDir = File(traitsDir, "_examples")
        if (!examplesDir.exists()) {
            generateExamples(examplesDir)
        }

        for (file in scanYamlFiles(traitsDir)) {
            if (file.parentFile?.name == "_examples") continue
            loadTrait(file)
        }

        plugin.logger.info("[TraitLoader] Loaded ${traits.size} traits")
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

    private fun loadTrait(file: File) {
        try {
            val yaml = YamlConfiguration.loadConfiguration(file)

            val id = yaml.getString("id") ?: run {
                plugin.logger.warning("[TraitLoader] Missing 'id' in ${file.name}")
                return
            }
            val displayName = yaml.getString("display-name") ?: id
            val description = yaml.getString("description") ?: ""

            val categoriesList = yaml.getStringList("categories")
            val categories = categoriesList.mapNotNull { WeaponCategory.fromString(it) }
            if (categories.isEmpty()) {
                plugin.logger.warning("[TraitLoader] No valid categories in ${file.name}")
                return
            }

            val effectsSection = yaml.getConfigurationSection("effects")
            val effects = mutableListOf<TraitEffect>()
            if (effectsSection != null) {
                for (key in effectsSection.getKeys(false)) {
                    val effectSection = effectsSection.getConfigurationSection(key) ?: continue
                    val typeName = effectSection.getString("type") ?: continue
                    val effectType = EffectType.fromString(typeName) ?: run {
                        plugin.logger.warning("[TraitLoader] Invalid effect type '$typeName' in ${file.name}")
                        continue
                    }

                    val parameters = mutableMapOf<String, Any>()
                    for (paramKey in effectSection.getKeys(false)) {
                        if (paramKey == "type") continue
                        val value = effectSection.get(paramKey)
                        if (value != null) {
                            parameters[paramKey] = value
                        }
                    }

                    effects.add(TraitEffect(effectType, parameters))
                }
            }

            val visualSection = yaml.getConfigurationSection("visual")
            val visual = if (visualSection != null) {
                TraitVisual(
                    type = visualSection.getString("type", "MAGIC")!!,
                    particle = visualSection.getString("particle", "WITCH")!!,
                    sound = visualSection.getString("sound", "ENTITY_EVOKER_CAST_SPELL")!!,
                    color = visualSection.getString("color")
                )
            } else {
                TraitVisual("MAGIC", "WITCH", "ENTITY_EVOKER_CAST_SPELL", null)
            }

            traits[id] = Trait(
                id = id,
                displayName = displayName,
                description = description,
                categories = categories,
                effects = effects,
                visual = visual
            )

            if (plugin.config.getBoolean("debug", false)) {
                plugin.logger.info("[TraitLoader] Loaded trait: $id (${effects.size} effects)")
            }
        } catch (e: Exception) {
            plugin.logger.warning("[TraitLoader] Failed to load ${file.name}: ${e.message}")
        }
    }

    private fun generateExamples(dir: File) {
        dir.mkdirs()

        val examples = listOf(
            Triple("rapid_strike", "Rapid Strike", listOf("KATANA", "LONGSWORD")),
            Triple("vampiric_edge", "Vampiric Edge", listOf("KATANA", "SPEAR")),
            Triple("frost_nova", "Frost Nova", listOf("KATANA", "LONGSWORD", "SPEAR")),
            Triple("thunder_clap", "Thunder Clap", listOf("HEAVY_HAMMER", "SPEAR")),
            Triple("ground_slam", "Ground Slam", listOf("HEAVY_HAMMER")),
            Triple("multi_shot", "Multi-Shot", listOf("CROSSBOW", "BOW")),
            Triple("explosive_arrow", "Explosive Arrow", listOf("BOW", "CROSSBOW")),
            Triple("arcane_burst", "Arcane Burst", listOf("STAFF")),
            Triple("shadow_step", "Shadow Step", listOf("DAGGER")),
            Triple("iron_will", "Iron Will", listOf("HEAVY_HAMMER", "LONGSWORD"))
        )

        for ((id, name, categories) in examples) {
            val file = File(dir, "_example_${id}.yml")
            if (file.exists()) continue

            val yaml = YamlConfiguration()
            yaml.set("id", id)
            yaml.set("display-name", name)
            yaml.set("description", "Example trait: $name")
            yaml.set("categories", categories)
            yaml.set("effects.0.type", "PASSIVE_DAMAGE")
            yaml.set("effects.0.bonus-damage", 5.0)
            yaml.set("visual.type", "MAGIC")
            yaml.set("visual.particle", "WITCH")
            yaml.set("visual.sound", "ENTITY_EVOKER_CAST_SPELL")

            val header = buildString {
                appendLine("CombatSystem - Example Trait")
                appendLine("Rename and move out of _examples/ to register.")
            }
            yaml.options().header(header)
            yaml.save(file)
        }

        plugin.logger.info("[TraitLoader] Generated example traits in ${dir.path}")
    }

    fun get(id: String): Trait? = traits[id]

    fun getAll(): Map<String, Trait> = traits.toMap()

    fun getByCategory(category: WeaponCategory): List<Trait> {
        return traits.values.filter { it.categories.contains(category) }
    }
}
