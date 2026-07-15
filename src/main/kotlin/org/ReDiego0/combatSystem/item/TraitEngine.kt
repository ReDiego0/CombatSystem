package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.config.ProgressionConfig
import org.ReDiego0.combatSystem.core.PDCKeys
import org.ReDiego0.combatSystem.core.PDCUtil
import org.ReDiego0.combatSystem.data.Rarity
import org.ReDiego0.combatSystem.data.Trait
import org.ReDiego0.combatSystem.data.WeaponCategory
import org.bukkit.inventory.ItemStack
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TraitEngine(
    private val traitLoader: TraitLoader,
    private val progressionConfig: ProgressionConfig
) {

    private val gson = Gson()

    fun generateTraits(item: ItemStack): ItemStack {
        val categoryName = PDCUtil.getString(item, "category") ?: return item
        val rarityName = PDCUtil.getString(item, "rarity") ?: return item
        val traitPoolId = PDCUtil.getString(item, "trait_pool") ?: return item
        val tier = PDCUtil.getInt(item, "tier") ?: 0

        val category = WeaponCategory.fromString(categoryName) ?: return item
        val rarity = Rarity.fromString(rarityName) ?: return item

        val availableTraits = traitLoader.getByCategory(category)
        if (availableTraits.isEmpty()) return item

        val columns = buildColumnStructure(rarity, tier)
        val generatedColumns = generateTraitColumns(availableTraits, columns)

        val columnsJson = gson.toJson(generatedColumns)
        var updatedItem = PDCUtil.setString(item, "trait_columns", columnsJson)
        updatedItem = PDCUtil.setString(updatedItem, "active_traits", "{}")

        return updatedItem
    }

    private fun buildColumnStructure(rarity: Rarity, tier: Int): List<Int> {
        if (!rarity.canHaveTier()) {
            return listOf(1)
        }

        return when (tier) {
            1 -> listOf(1)
            2 -> listOf(2)
            3 -> listOf(2, 1)
            4 -> listOf(2, 2)
            5 -> listOf(2, 2)
            else -> listOf(1)
        }
    }

    private fun generateTraitColumns(
        availableTraits: List<Trait>,
        columns: List<Int>
    ): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val usedTraitIds = mutableSetOf<String>()

        for (rowCount in columns) {
            val column = mutableListOf<String>()
            val shuffled = availableTraits.filter { it.id !in usedTraitIds }.shuffled()

            for (i in 0 until rowCount) {
                if (i < shuffled.size) {
                    column.add(shuffled[i].id)
                    usedTraitIds.add(shuffled[i].id)
                }
            }
            result.add(column)
        }

        return result
    }

    fun getTraitColumns(item: ItemStack): List<List<String>> {
        val json = PDCUtil.getString(item, "trait_columns") ?: return emptyList()
        val type = object : TypeToken<List<List<String>>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getActiveTraits(item: ItemStack): Map<String, String> {
        val json = PDCUtil.getString(item, "active_traits") ?: return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun setActiveTrait(item: ItemStack, columnIndex: Int, traitId: String): ItemStack {
        val activeTraits = getActiveTraits(item).toMutableMap()
        activeTraits[columnIndex.toString()] = traitId
        val json = gson.toJson(activeTraits)
        return PDCUtil.setString(item, "active_traits", json)
    }

    fun isTraitUnlocked(item: ItemStack, columnIndex: Int, rowIndex: Int): Boolean {
        val level = PDCUtil.getInt(item, "level") ?: 0
        val columns = getTraitColumns(item)

        val totalTraits = columns.sumOf { it.size }
        if (totalTraits == 0) return false

        val unlockLevels = calculateUnlockLevels(totalTraits)
        val traitIndex = getTraitLinearIndex(columns, columnIndex, rowIndex)

        return traitIndex in unlockLevels.indices && level >= unlockLevels[traitIndex]
    }

    private fun calculateUnlockLevels(totalTraits: Int): List<Int> {
        val maxLevel = progressionConfig.getMaxLevel()
        if (totalTraits <= 1) return listOf(maxLevel / 2)

        val step = maxLevel.toDouble() / totalTraits
        return (0 until totalTraits).map { ((it + 1) * step).toInt().coerceIn(1, maxLevel) }
    }

    private fun getTraitLinearIndex(columns: List<List<String>>, columnIndex: Int, rowIndex: Int): Int {
        var index = 0
        for (col in 0 until columnIndex) {
            index += columns[col].size
        }
        return index + rowIndex
    }

    fun getTraitUnlockLevel(item: ItemStack, columnIndex: Int, rowIndex: Int): Int {
        val columns = getTraitColumns(item)
        val totalTraits = columns.sumOf { it.size }
        val unlockLevels = calculateUnlockLevels(totalTraits)
        val traitIndex = getTraitLinearIndex(columns, columnIndex, rowIndex)

        return if (traitIndex in unlockLevels.indices) unlockLevels[traitIndex] else -1
    }

    fun getTraitProgress(item: ItemStack, columnIndex: Int, rowIndex: Int): Pair<Int, Int> {
        val level = PDCUtil.getInt(item, "level") ?: 0
        val unlockLevel = getTraitUnlockLevel(item, columnIndex, rowIndex)
        return Pair(level, unlockLevel)
    }
}
