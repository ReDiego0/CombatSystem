package org.ReDiego0.combatSystem.gui

import org.ReDiego0.combatSystem.core.PDCUtil
import org.ReDiego0.combatSystem.data.Rarity
import org.ReDiego0.combatSystem.data.Trait
import org.ReDiego0.combatSystem.data.WeaponCategory
import org.ReDiego0.combatSystem.item.TraitEngine
import org.ReDiego0.combatSystem.item.TraitLoader
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ArmoryGUI(
    private val traitEngine: TraitEngine,
    private val traitLoader: TraitLoader
) {

    companion object {
        const val INVENTORY_SIZE = 54
        const val TITLE_PREFIX = "§6§lARMORY - "
        const val TRAIT_SLOTS_PER_COLUMN = 2
        val COLUMN_SLOTS = listOf(11 to 12, 14 to 15)
        val FILLER_SLOTS = (0..53).toList()
    }

    fun open(player: Player, item: ItemStack) {
        val displayName = PDCUtil.getString(item, "item_id") ?: "Unknown"
        val rarityName = PDCUtil.getString(item, "rarity") ?: "COMMON"
        val rarity = Rarity.fromString(rarityName) ?: Rarity.COMMON
        val category = PDCUtil.getString(item, "category") ?: "UNKNOWN"

        val title = "$TITLE_PREFIX${rarity.colorCode}$displayName"
        val inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title)

        val columns = traitEngine.getTraitColumns(item)
        val activeTraits = traitEngine.getActiveTraits(item)

        fillBackground(inventory)

        for ((colIndex, column) in columns.withIndex()) {
            if (colIndex >= COLUMN_SLOTS.size) break
            val (slot1, slot2) = COLUMN_SLOTS[colIndex]

            for ((rowIndex, traitId) in column.withIndex()) {
                val trait = traitLoader.get(traitId)
                val slot = if (rowIndex == 0) slot1 else slot2
                val isUnlocked = traitEngine.isTraitUnlocked(item, colIndex, rowIndex)
                val isActive = activeTraits[colIndex.toString()] == traitId

                val traitItem = createTraitItem(trait, isUnlocked, isActive, item, colIndex, rowIndex)
                inventory.setItem(slot, traitItem)
            }
        }

        player.openInventory(inventory)
    }

    private fun fillBackground(inventory: Inventory) {
        val filler = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        val meta = filler.itemMeta
        meta?.setDisplayName(" ")
        filler.itemMeta = meta

        for (slot in FILLER_SLOTS) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler)
            }
        }
    }

    private fun createTraitItem(
        trait: Trait?,
        isUnlocked: Boolean,
        isActive: Boolean,
        item: ItemStack,
        columnIndex: Int,
        rowIndex: Int
    ): ItemStack {
        if (trait == null) {
            val unknown = ItemStack(Material.BARRIER)
            val meta = unknown.itemMeta
            meta?.setDisplayName("§cUnknown Trait")
            unknown.itemMeta = meta
            return unknown
        }

        val material = when {
            isActive -> Material.LIME_STAINED_GLASS_PANE
            isUnlocked -> Material.YELLOW_STAINED_GLASS_PANE
            else -> Material.RED_STAINED_GLASS_PANE
        }

        val traitItem = ItemStack(material)
        val meta = traitItem.itemMeta ?: return traitItem

        val statusPrefix = when {
            isActive -> "§a✅ "
            isUnlocked -> "§e🔓 "
            else -> "§c🔒 "
        }

        meta.setDisplayName("$statusPrefix§f${trait.displayName}")

        val lore = mutableListOf<String>()
        lore.add("§7${trait.description}")
        lore.add("§7───────────────")

        for (effect in trait.effects) {
            lore.add("§7• §f${formatEffect(effect.type, effect.parameters)}")
        }

        lore.add("§7───────────────")

        if (!isUnlocked) {
            val (currentLevel, unlockLevel) = traitEngine.getTraitProgress(item, columnIndex, rowIndex)
            lore.add("§7Unlock: §fLv. $unlockLevel")
            lore.add("§7Progress: §a$currentLevel/$unlockLevel")
        } else if (isActive) {
            lore.add("§a§lACTIVE")
        } else {
            lore.add("§eClick to activate")
        }

        meta.lore = lore
        traitItem.itemMeta = meta

        return traitItem
    }

    private fun formatEffect(type: org.ReDiego0.combatSystem.data.EffectType, params: Map<String, Any>): String {
        return when (type) {
            org.ReDiego0.combatSystem.data.EffectType.COMBO_DAMAGE -> {
                val count = (params["trigger-count"] as? Number)?.toInt() ?: 3
                val mult = ((params["damage-multiplier"] as? Number)?.toDouble() ?: 1.4) * 100
                "Every ${count}th hit: +${mult.toInt()}% damage"
            }
            org.ReDiego0.combatSystem.data.EffectType.CRITICAL_BOOST -> {
                val chance = ((params["chance"] as? Number)?.toDouble() ?: 0.1) * 100
                "+${chance.toInt()}% critical chance"
            }
            org.ReDiego0.combatSystem.data.EffectType.LIFESTEAL -> {
                val pct = ((params["steal-percentage"] as? Number)?.toDouble() ?: 0.05) * 100
                "Steals ${pct.toInt()}% damage as HP"
            }
            org.ReDiego0.combatSystem.data.EffectType.SLOW_ON_HIT -> {
                val chance = ((params["chance"] as? Number)?.toDouble() ?: 0.15) * 100
                val dur = (params["duration"] as? Number)?.toDouble() ?: 2.0
                "${chance.toInt()}% slow for ${dur}s"
            }
            org.ReDiego0.combatSystem.data.EffectType.STUN_ON_HIT -> {
                val chance = ((params["chance"] as? Number)?.toDouble() ?: 0.1) * 100
                val dur = (params["duration"] as? Number)?.toDouble() ?: 1.5
                "${chance.toInt()}% stun for ${dur}s"
            }
            org.ReDiego0.combatSystem.data.EffectType.AOE_DAMAGE -> {
                val radius = (params["radius"] as? Number)?.toDouble() ?: 3.0
                val pct = ((params["damage-percentage"] as? Number)?.toDouble() ?: 0.25) * 100
                "AoE ${pct.toInt()}% in ${radius}b radius"
            }
            org.ReDiego0.combatSystem.data.EffectType.PASSIVE_DAMAGE -> {
                val bonus = (params["bonus-damage"] as? Number)?.toDouble() ?: 5.0
                "+${bonus} damage"
            }
            org.ReDiego0.combatSystem.data.EffectType.PASSIVE_SPEED -> {
                val pct = ((params["speed-bonus"] as? Number)?.toDouble() ?: 0.1) * 100
                "+${pct.toInt()}% attack speed"
            }
            org.ReDiego0.combatSystem.data.EffectType.HEAL_ON_KILL -> {
                val amount = (params["heal-amount"] as? Number)?.toDouble() ?: 10.0
                "Heals ${amount} HP on kill"
            }
            org.ReDiego0.combatSystem.data.EffectType.DAMAGE_REDUCTION -> {
                val pct = ((params["reduction-percentage"] as? Number)?.toDouble() ?: 0.1) * 100
                "-${pct.toInt()}% damage taken"
            }
            else -> type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
