package org.ReDiego0.combatSystem.combat.skills

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object LongswordSkills {

    fun executeLunge(player: Player) {
        val direction = player.location.direction.normalize()
        val dashVector = direction.multiply(5.0)
        player.velocity = dashVector

        val nearbyEntities = player.getNearbyEntities(2.0, 2.0, 2.0)
        for (entity in nearbyEntities) {
            if (entity is LivingEntity && entity != player) {
                val dot = direction.dot(entity.location.toVector().subtract(player.location.toVector()).normalize())
                if (dot > 0.7) {
                    entity.damage(14.0, player)
                }
            }
        }
    }

    fun executeBladeGuard(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 40, 3))
    }
}
