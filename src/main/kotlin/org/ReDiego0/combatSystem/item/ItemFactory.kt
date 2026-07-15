package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.core.PDCKeys
import org.ReDiego0.combatSystem.core.PDCUtil
import org.ReDiego0.combatSystem.data.Rarity
import org.ReDiego0.combatSystem.data.WeaponCategory
import org.ReDiego0.combatSystem.data.WeaponData
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object ItemFactory {

    fun createWeapon(weaponData: WeaponData): ItemStack {
        val item = ItemStack(weaponData.category.material)
        val meta = item.itemMeta ?: return item

        meta.setDisplayName("${weaponData.rarity.colorCode}${weaponData.rarity.loreSymbol} ${weaponData.displayName}")

        meta.isUnbreakable = true

        applyPDC(meta, weaponData)

        applyVanillaAttributes(meta, weaponData)

        item.itemMeta = meta

        return item
    }

    private fun applyPDC(meta: ItemMeta, weaponData: WeaponData) {
        val container = meta.persistentDataContainer

        container.set(PDCKeys.ITEM_ID, org.bukkit.persistence.PersistentDataType.STRING, weaponData.id)
        container.set(PDCKeys.CATEGORY, org.bukkit.persistence.PersistentDataType.STRING, weaponData.category.name)
        container.set(PDCKeys.RARITY, org.bukkit.persistence.PersistentDataType.STRING, weaponData.rarity.name)
        container.set(PDCKeys.LEVEL, org.bukkit.persistence.PersistentDataType.INTEGER, 0)
        container.set(PDCKeys.TIER, org.bukkit.persistence.PersistentDataType.INTEGER, weaponData.tier)
        container.set(PDCKeys.IS_MANAGED, org.bukkit.persistence.PersistentDataType.BYTE, 1)
        container.set(PDCKeys.BASE_DAMAGE, org.bukkit.persistence.PersistentDataType.DOUBLE, weaponData.baseDamage)
        container.set(PDCKeys.ATTACK_SPEED, org.bukkit.persistence.PersistentDataType.DOUBLE, weaponData.attackSpeed)
        container.set(PDCKeys.PHYSICAL_DAMAGE, org.bukkit.persistence.PersistentDataType.DOUBLE, weaponData.baseDamage)
        container.set(PDCKeys.ITEM_XP, org.bukkit.persistence.PersistentDataType.DOUBLE, 0.0)

        if (weaponData.traitPool != null) {
            container.set(PDCKeys.TRAIT_POOL, org.bukkit.persistence.PersistentDataType.STRING, weaponData.traitPool)
        }
    }

    private fun applyVanillaAttributes(meta: ItemMeta, weaponData: WeaponData) {
        meta.addAttributeModifier(
            Attribute.ATTACK_DAMAGE,
            AttributeModifier(
                PDCKeys.BASE_DAMAGE,
                weaponData.baseDamage - 1.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            )
        )
        meta.addAttributeModifier(
            Attribute.ATTACK_SPEED,
            AttributeModifier(
                PDCKeys.ATTACK_SPEED,
                weaponData.attackSpeed - 4.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            )
        )
    }

    fun buildLore(item: ItemStack, progression: ItemProgression? = null): ItemStack {
        val meta = item.itemMeta ?: return item

        val rarityName = PDCUtil.getString(item, "rarity") ?: return item
        val category = PDCUtil.getString(item, "category") ?: return item
        val rarity = Rarity.fromString(rarityName) ?: return item
        val level = PDCUtil.getInt(item, "level") ?: 0
        val tier = PDCUtil.getInt(item, "tier") ?: 0
        val baseDamage = PDCUtil.getDouble(item, "base_damage") ?: return item
        val baseAttackSpeed = PDCUtil.getDouble(item, "attack_speed") ?: return item

        val currentDamage = if (progression != null) {
            progression.getScaledDamage(baseDamage, level, rarity)
        } else {
            baseDamage
        }
        val currentAttackSpeed = if (progression != null) {
            progression.getScaledAttackSpeed(baseAttackSpeed, level, rarity)
        } else {
            baseAttackSpeed
        }

        val displayName = "${rarity.colorCode}${rarity.loreSymbol} ${rarity.displayName} $category"
        meta.setDisplayName(displayName)

        val lore = mutableListOf<String>()
        lore.add("§7Lv. §f${level}/40 §7| ${rarity.colorCode}${rarity.displayName}")
        if (rarity.canHaveTier()) {
            lore.add("§7| §eTier $tier")
        }
        lore.add("§7───────────────")
        lore.add("§7Damage: §c${"%.1f".format(currentDamage)} §7| Speed: §b${"%.1f".format(currentAttackSpeed)}")

        if (progression != null && level < 40) {
            val xp = PDCUtil.getDouble(item, "item_xp") ?: 0.0
            val requiredXP = progression.getRequiredXP(level)
            val percentage = ((xp / requiredXP) * 100).toInt().coerceIn(0, 100)
            lore.add("§7XP: §a${"%.0f".format(xp)}/${"%.0f".format(requiredXP)} §7($percentage%)")
        } else if (level >= 40) {
            lore.add("§7XP: §aMAX LEVEL")
        }

        meta.lore = lore
        item.itemMeta = meta

        return item
    }

    fun isManagedWeapon(item: ItemStack): Boolean {
        return PDCUtil.getBoolean(item, "is_managed")
    }

    fun getWeaponData(item: ItemStack): WeaponData? {
        if (!isManagedWeapon(item)) return null

        val id = PDCUtil.getString(item, "item_id") ?: return null
        val categoryName = PDCUtil.getString(item, "category") ?: return null
        val rarityName = PDCUtil.getString(item, "rarity") ?: return null

        val category = WeaponCategory.fromString(categoryName) ?: return null
        val rarity = Rarity.fromString(rarityName) ?: return null

        return WeaponData(
            id = id,
            displayName = PDCUtil.getString(item, "item_id") ?: id,
            category = category,
            rarity = rarity,
            tier = PDCUtil.getInt(item, "tier") ?: 0,
            baseDamage = PDCUtil.getDouble(item, "base_damage") ?: category.baseDamage,
            attackSpeed = PDCUtil.getDouble(item, "attack_speed") ?: category.attackSpeed,
            traitPool = PDCUtil.getString(item, "trait_pool"),
            lootPools = emptyList()
        )
    }

    fun getLevel(item: ItemStack): Int {
        return PDCUtil.getInt(item, "level") ?: 0
    }

    fun getXP(item: ItemStack): Double {
        return PDCUtil.getDouble(item, "item_xp") ?: 0.0
    }
}
