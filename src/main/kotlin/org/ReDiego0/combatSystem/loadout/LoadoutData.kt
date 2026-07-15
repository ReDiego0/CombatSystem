package org.ReDiego0.combatSystem.loadout

import org.bukkit.inventory.ItemStack

data class LoadoutData(
    val weapon1: ItemStack? = null,
    val weapon2: ItemStack? = null,
    val helmet: ItemStack? = null,
    val chestplate: ItemStack? = null,
    val leggings: ItemStack? = null,
    val boots: ItemStack? = null,
    val supportItems: List<ItemStack?> = listOf(null, null, null, null, null, null),
    val isEquipped: Boolean = false
)
