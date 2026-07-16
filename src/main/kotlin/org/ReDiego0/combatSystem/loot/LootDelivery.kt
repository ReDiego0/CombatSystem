package org.ReDiego0.combatSystem.loot

import org.ReDiego0.combatSystem.loadout.LoadoutManager
import org.ReDiego0.combatSystem.item.WeaponRegistry
import org.ReDiego0.combatSystem.item.ArmorLoader
import org.ReDiego0.combatSystem.config.SupportItemConfig
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class LootDelivery(
    private val plugin: JavaPlugin,
    private val loadoutManager: LoadoutManager,
    private val weaponRegistry: WeaponRegistry,
    private val armorLoader: ArmorLoader,
    private val supportItemConfig: SupportItemConfig,
    private val lostLootStash: LostLootStash
) {

    fun deliverWeapon(player: Player, weaponId: String): Boolean {
        val weaponData = weaponRegistry.get(weaponId) ?: return false
        val item = org.ReDiego0.combatSystem.item.ItemFactory.createWeapon(weaponData)

        val loadout = loadoutManager.getLoadout(player.uniqueId)
        if (loadout != null) {
            if (loadout.weapon1 == null) {
                loadoutManager.updateWeapon(player.uniqueId, 0, item)
            } else if (loadout.weapon2 == null) {
                loadoutManager.updateWeapon(player.uniqueId, 1, item)
            } else {
                lostLootStash.addItem(player.uniqueId, item)
                player.sendMessage("§e[Loot] Weapon sent to Lost Loot Stash (loadout full)")
                return true
            }
            player.sendMessage("§a[Loot] Weapon added to loadout")
            return true
        }

        lostLootStash.addItem(player.uniqueId, item)
        player.sendMessage("§e[Loot] Weapon sent to Lost Loot Stash")
        return true
    }

    fun deliverArmor(player: Player, armorId: String): Boolean {
        val armorData = armorLoader.get(armorId) ?: return false
        val item = createArmorItem(armorData)

        val loadout = loadoutManager.getLoadout(player.uniqueId)
        if (loadout != null) {
            val slot = armorData.setPiece?.ordinal ?: -1
            if (slot in 0..3) {
                val currentArmor = when (slot) {
                    0 -> loadout.helmet
                    1 -> loadout.chestplate
                    2 -> loadout.leggings
                    3 -> loadout.boots
                    else -> null
                }
                if (currentArmor == null) {
                    loadoutManager.updateArmor(player.uniqueId, slot, item)
                    player.sendMessage("§a[Loot] Armor added to loadout")
                    return true
                }
            }
        }

        lostLootStash.addItem(player.uniqueId, item)
        player.sendMessage("§e[Loot] Armor sent to Lost Loot Stash")
        return true
    }

    fun deliverConsumable(player: Player, itemId: String): Boolean {
        val supportItem = supportItemConfig.getSupportItems()[itemId] ?: return false
        val item = supportItem.toItemStack()

        val emptySlot = player.inventory.firstEmpty()
        if (emptySlot != -1) {
            player.inventory.setItem(emptySlot, item)
            player.sendMessage("§a[Loot] Item added to inventory")
            return true
        }

        lostLootStash.addItem(player.uniqueId, item)
        player.sendMessage("§e[Loot] Item sent to Lost Loot Stash (inventory full)")
        return true
    }

    private fun createArmorItem(armorData: org.ReDiego0.combatSystem.data.ArmorData): ItemStack {
        val item = ItemStack(armorData.material)
        val meta = item.itemMeta

        meta?.setDisplayName("${armorData.rarity.colorCode}${armorData.rarity.loreSymbol} ${armorData.displayName}")
        meta?.isUnbreakable = true

        val container = meta?.persistentDataContainer
        container?.set(org.ReDiego0.combatSystem.core.PDCKeys.ITEM_ID, org.bukkit.persistence.PersistentDataType.STRING, armorData.id)
        container?.set(org.ReDiego0.combatSystem.core.PDCKeys.RARITY, org.bukkit.persistence.PersistentDataType.STRING, armorData.rarity.name)
        container?.set(org.ReDiego0.combatSystem.core.PDCKeys.IS_MANAGED, org.bukkit.persistence.PersistentDataType.BYTE, 1)
        container?.set(org.ReDiego0.combatSystem.core.PDCKeys.DEFENSE, org.bukkit.persistence.PersistentDataType.DOUBLE, armorData.defense)
        container?.set(org.ReDiego0.combatSystem.core.PDCKeys.HP_BONUS, org.bukkit.persistence.PersistentDataType.DOUBLE, armorData.hpBonus)

        if (armorData.setId != null) {
            container?.set(org.ReDiego0.combatSystem.core.PDCUtil.key("set_id"), org.bukkit.persistence.PersistentDataType.STRING, armorData.setId)
        }

        item.itemMeta = meta
        return item
    }
}
