package org.ReDiego0.combatSystem.combat

import org.ReDiego0.combatSystem.core.PDCUtil
import org.ReDiego0.combatSystem.data.Rarity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.Particle

object UltimateExecutor {

    fun executeUltimate(player: Player) {
        val item = player.inventory.itemInMainHand
        if (!PDCUtil.hasCustomData(item)) return

        val rarityName = PDCUtil.getString(item, "rarity") ?: return
        val rarity = Rarity.fromString(rarityName) ?: return

        if (rarity != Rarity.MYTHIC) return

        val nearbyEntities = player.getNearbyEntities(6.0, 6.0, 6.0)
        for (entity in nearbyEntities) {
            if (entity is LivingEntity && entity != player) {
                entity.damage(50.0, player)

                entity.addPotionEffect(
                    org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOWNESS,
                        80,
                        2
                    )
                )
            }
        }

        player.world.spawnParticle(
            Particle.SONIC_BOOM,
            player.location,
            1
        )
    }

    fun canUseUltimate(player: Player): Boolean {
        val item = player.inventory.itemInMainHand
        if (!PDCUtil.hasCustomData(item)) return false

        val rarityName = PDCUtil.getString(item, "rarity") ?: return false
        val rarity = Rarity.fromString(rarityName) ?: return false

        return rarity == Rarity.MYTHIC
    }
}
