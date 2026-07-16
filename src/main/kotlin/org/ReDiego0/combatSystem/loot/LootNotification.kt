package org.ReDiego0.combatSystem.loot

import org.ReDiego0.combatSystem.data.Rarity
import org.bukkit.Sound
import org.bukkit.entity.Player

class LootNotification {

    fun sendWeaponNotification(player: Player, weaponName: String, rarity: Rarity) {
        val color = rarity.colorCode
        val message = "§7[!] Reward obtained: $color$weaponName"
        player.sendMessage(message)
        playRaritySound(player, rarity)
    }

    fun sendArmorNotification(player: Player, armorName: String, rarity: Rarity) {
        val color = rarity.colorCode
        val message = "§7[!] Reward obtained: $color$armorName"
        player.sendMessage(message)
        playRaritySound(player, rarity)
    }

    fun sendConsumableNotification(player: Player, itemName: String) {
        player.sendMessage("§7[!] Reward obtained: §f$itemName")
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
    }

    fun sendStashNotification(player: Player) {
        player.sendMessage("§e[Loot] Your inventory was full. Reward secured in Recovery Stash.")
    }

    private fun playRaritySound(player: Player, rarity: Rarity) {
        val sound = when (rarity) {
            Rarity.COMMON -> Sound.ENTITY_EXPERIENCE_ORB_PICKUP
            Rarity.UNCOMMON -> Sound.ENTITY_PLAYER_LEVELUP
            Rarity.RARE -> Sound.ENTITY_ENDER_DRAGON_GROWL
            Rarity.LEGENDARY -> Sound.ENTITY_WITHER_SPAWN
            Rarity.MYTHIC -> Sound.ENTITY_ENDER_DRAGON_DEATH
        }
        val volume = when (rarity) {
            Rarity.COMMON, Rarity.UNCOMMON -> 0.5f
            Rarity.RARE -> 0.8f
            Rarity.LEGENDARY, Rarity.MYTHIC -> 1.0f
        }
        val pitch = when (rarity) {
            Rarity.COMMON -> 1.5f
            Rarity.UNCOMMON -> 1.3f
            Rarity.RARE -> 1.1f
            Rarity.LEGENDARY -> 1.0f
            Rarity.MYTHIC -> 0.8f
        }
        player.playSound(player.location, sound, volume, pitch)
    }
}
