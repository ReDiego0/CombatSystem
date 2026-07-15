package org.ReDiego0.combatSystem.item

import org.ReDiego0.combatSystem.data.ArmorData
import org.ReDiego0.combatSystem.data.ArmorSet
import org.ReDiego0.combatSystem.data.Rarity
import org.ReDiego0.combatSystem.core.PDCUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ArmorRegistry(
    private val armorLoader: ArmorLoader,
    private val bonusLoader: ArmorBonusLoader
) {

    private val equippedArmor = ConcurrentHashMap<UUID, Map<ArmorData.ArmorSlot, ArmorData?>>()

    fun updateEquippedArmor(player: Player) {
        val uuid = player.uniqueId
        val armorMap = mutableMapOf<ArmorData.ArmorSlot, ArmorData?>()

        armorMap[ArmorData.ArmorSlot.HELMET] = getArmorFromItem(player.inventory.helmet)
        armorMap[ArmorData.ArmorSlot.CHESTPLATE] = getArmorFromItem(player.inventory.chestplate)
        armorMap[ArmorData.ArmorSlot.LEGGINGS] = getArmorFromItem(player.inventory.leggings)
        armorMap[ArmorData.ArmorSlot.BOOTS] = getArmorFromItem(player.inventory.boots)

        equippedArmor[uuid] = armorMap
    }

    private fun getArmorFromItem(item: ItemStack?): ArmorData? {
        if (item == null || item.type == Material.AIR) return null
        if (!PDCUtil.hasCustomData(item)) return null

        val armorId = PDCUtil.getString(item, "armor_id") ?: return null
        return armorLoader.get(armorId)
    }

    fun getEquippedArmor(player: Player): Map<ArmorData.ArmorSlot, ArmorData?> {
        return equippedArmor[player.uniqueId] ?: emptyMap()
    }

    fun getEquippedSets(player: Player): List<ArmorSet> {
        val armor = getEquippedArmor(player)
        val sets = mutableMapOf<String, MutableList<ArmorData>>()

        for ((_, armorData) in armor) {
            if (armorData?.setId != null) {
                sets.getOrPut(armorData.setId) { mutableListOf() }.add(armorData)
            }
        }

        return sets.map { (setId, pieces) -> ArmorSet(setId, pieces) }
    }

    fun removePlayer(player: Player) {
        equippedArmor.remove(player.uniqueId)
    }
}
