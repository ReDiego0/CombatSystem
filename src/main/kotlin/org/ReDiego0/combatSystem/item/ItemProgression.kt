package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.config.ProgressionConfig
import org.ReDiego0.combatSystem.core.PDCKeys
import org.ReDiego0.combatSystem.core.PDCUtil
import org.ReDiego0.combatSystem.data.ItemStats
import org.ReDiego0.combatSystem.data.Rarity
import org.ReDiego0.combatSystem.data.WeaponData
import org.bukkit.inventory.ItemStack
import kotlin.math.pow

class ItemProgression(
    private val progressionConfig: ProgressionConfig
) {

    fun addXP(item: ItemStack, amount: Double): ItemStack {
        if (!PDCUtil.hasCustomData(item)) return item

        var currentXP = PDCUtil.getDouble(item, "item_xp") ?: 0.0
        var currentLevel = PDCUtil.getInt(item, "level") ?: 0
        val maxLevel = progressionConfig.getMaxLevel()

        if (currentLevel >= maxLevel) return item

        currentXP += amount

        var updatedItem = item
        while (currentLevel < maxLevel) {
            val required = getRequiredXP(currentLevel)
            if (currentXP >= required) {
                currentXP -= required
                currentLevel++
                updatedItem = PDCUtil.setInt(updatedItem, "level", currentLevel)
                updatedItem = applyStats(updatedItem, currentLevel)
            } else {
                break
            }
        }

        updatedItem = PDCUtil.setDouble(updatedItem, "item_xp", currentXP)
        updatedItem = rebuildLore(updatedItem)

        return updatedItem
    }

    fun getRequiredXP(level: Int): Double {
        val base = progressionConfig.getXPBase()
        val exponent = progressionConfig.getXPExponent()
        return base * ((level + 1).toDouble().pow(exponent))
    }

    fun getScaledDamage(baseDamage: Double, level: Int, rarity: Rarity): Double {
        val growth = progressionConfig.getDamageGrowth()
        val multiplier = progressionConfig.getRarityMultiplier(rarity)
        return baseDamage + (growth * multiplier * level)
    }

    fun getScaledAttackSpeed(baseAttackSpeed: Double, level: Int, rarity: Rarity): Double {
        val growth = progressionConfig.getAttackSpeedGrowth()
        val multiplier = progressionConfig.getRarityMultiplier(rarity)
        return baseAttackSpeed + (growth * multiplier * level)
    }

    fun getCurrentStats(item: ItemStack): ItemStats? {
        if (!PDCUtil.hasCustomData(item)) return null

        val baseDamage = PDCUtil.getDouble(item, "base_damage") ?: return null
        val baseAttackSpeed = PDCUtil.getDouble(item, "attack_speed") ?: return null
        val level = PDCUtil.getInt(item, "level") ?: 0
        val xp = PDCUtil.getDouble(item, "item_xp") ?: 0.0
        val rarityName = PDCUtil.getString(item, "rarity") ?: return null
        val rarity = Rarity.fromString(rarityName) ?: return null

        return ItemStats(
            damage = getScaledDamage(baseDamage, level, rarity),
            attackSpeed = getScaledAttackSpeed(baseAttackSpeed, level, rarity),
            level = level,
            xp = xp,
            requiredXP = getRequiredXP(level)
        )
    }

    fun applyStats(item: ItemStack, level: Int): ItemStack {
        val baseDamage = PDCUtil.getDouble(item, "base_damage") ?: return item
        val baseAttackSpeed = PDCUtil.getDouble(item, "attack_speed") ?: return item
        val rarityName = PDCUtil.getString(item, "rarity") ?: return item
        val rarity = Rarity.fromString(rarityName) ?: return item

        val scaledDamage = getScaledDamage(baseDamage, level, rarity)
        val scaledAttackSpeed = getScaledAttackSpeed(baseAttackSpeed, level, rarity)

        var updatedItem = PDCUtil.setDouble(item, "phys_dmg", scaledDamage)
        updatedItem = PDCUtil.setDouble(updatedItem, "attack_speed", scaledAttackSpeed)

        return updatedItem
    }

    fun rebuildLore(item: ItemStack): ItemStack {
        val meta = item.itemMeta ?: return item

        val itemId = PDCUtil.getString(item, "item_id") ?: return item
        val rarityName = PDCUtil.getString(item, "rarity") ?: return item
        val category = PDCUtil.getString(item, "category") ?: return item
        val rarity = Rarity.fromString(rarityName) ?: return item
        val level = PDCUtil.getInt(item, "level") ?: 0
        val tier = PDCUtil.getInt(item, "tier") ?: 0
        val stats = getCurrentStats(item) ?: return item

        val displayName = "${rarity.colorCode}${rarity.loreSymbol} ${rarity.displayName} $category"
        meta.setDisplayName(displayName)

        val lore = mutableListOf<String>()
        lore.add("§7Lv. §f${level}/40 §7| ${rarity.colorCode}${rarity.displayName}")
        if (rarity.canHaveTier()) {
            lore.add("§7| §eTier $tier")
        }
        lore.add("§7───────────────")
        lore.add("§7Damage: §c${"%.1f".format(stats.damage)} §7| Speed: §b${"%.1f".format(stats.attackSpeed)}")

        val requiredXP = stats.requiredXP
        if (level < progressionConfig.getMaxLevel()) {
            val percentage = ((stats.xp / requiredXP) * 100).toInt().coerceIn(0, 100)
            lore.add("§7XP: §a${"%.0f".format(stats.xp)}/${"%.0f".format(requiredXP)} §7($percentage%)")
        } else {
            lore.add("§7XP: §aMAX LEVEL")
        }

        meta.lore = lore
        item.itemMeta = meta

        return item
    }

    fun calculateMobXP(mobLevel: Int): Double {
        val baseXP = progressionConfig.getMobKillBaseXP()
        return baseXP * (1.0 + mobLevel * 0.1)
    }

    fun distributeXP(item: ItemStack, xp: Double): Pair<ItemStack, Double> {
        val weaponShare = progressionConfig.getWeaponShare()
        val weaponXP = xp * weaponShare
        return Pair(addXP(item, weaponXP), weaponXP)
    }
}
