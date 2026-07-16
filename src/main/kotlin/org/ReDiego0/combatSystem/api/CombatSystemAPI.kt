package org.ReDiego0.combatSystem.api

import org.ReDiego0.combatSystem.core.PDCUtil
import org.ReDiego0.combatSystem.data.Rarity
import org.ReDiego0.combatSystem.data.WeaponCategory
import org.ReDiego0.combatSystem.item.ItemFactory
import org.bukkit.inventory.ItemStack

object CombatSystemAPI {

    fun getRarity(item: ItemStack): Rarity? {
        val rarityName = PDCUtil.getString(item, "rarity") ?: return null
        return Rarity.fromString(rarityName)
    }

    fun getItemCategory(item: ItemStack): WeaponCategory? {
        val categoryName = PDCUtil.getString(item, "category") ?: return null
        return WeaponCategory.fromString(categoryName)
    }

    fun getItemLevel(item: ItemStack): Int {
        return PDCUtil.getInt(item, "level") ?: 0
    }

    fun getItemTier(item: ItemStack): Int {
        return PDCUtil.getInt(item, "tier") ?: 0
    }

    fun getInternalID(item: ItemStack): String? {
        return PDCUtil.getString(item, "item_id")
    }

    fun isManagedWeapon(item: ItemStack): Boolean {
        return ItemFactory.isManagedWeapon(item)
    }

    fun isManagedArmor(item: ItemStack): Boolean {
        if (!PDCUtil.hasCustomData(item)) return false
        return PDCUtil.getString(item, "set_id") != null || PDCUtil.getString(item, "armor_id") != null
    }

    fun getSetId(item: ItemStack): String? {
        return PDCUtil.getString(item, "set_id")
    }

    fun getArmorId(item: ItemStack): String? {
        return PDCUtil.getString(item, "armor_id")
    }

    fun getBaseDamage(item: ItemStack): Double? {
        return PDCUtil.getDouble(item, "base_damage")
    }

    fun getAttackSpeed(item: ItemStack): Double? {
        return PDCUtil.getDouble(item, "attack_speed")
    }

    fun getDefense(item: ItemStack): Double? {
        return PDCUtil.getDouble(item, "defense")
    }

    fun getHpBonus(item: ItemStack): Double? {
        return PDCUtil.getDouble(item, "hp_bonus")
    }

    fun getItemXP(item: ItemStack): Double {
        return PDCUtil.getDouble(item, "item_xp") ?: 0.0
    }

    fun getTraitPoolId(item: ItemStack): String? {
        return PDCUtil.getString(item, "trait_pool")
    }
}
