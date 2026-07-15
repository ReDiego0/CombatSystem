package org.ReDiego0.combatSystem.combat.skills

import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object CrossbowSkills {

    fun executeMultiShot(player: Player) {
        val direction = player.location.direction

        for (i in -1..1) {
            val spread = direction.clone().add(
                org.bukkit.util.Vector(
                    i.toDouble() * 0.15,
                    0.0,
                    i.toDouble() * 0.15
                )
            ).normalize()

            val arrow = player.launchProjectile(Arrow::class.java, spread.multiply(2.0))
            arrow.damage = 4.0
        }
    }

    fun executeNetShot(player: Player) {
        val direction = player.location.direction.normalize()
        val startLoc = player.eyeLocation

        val arrow = player.launchProjectile(Arrow::class.java, direction.multiply(2.5))
        arrow.damage = 6.0

        object : org.bukkit.scheduler.BukkitRunnable() {
            override fun run() {
                if (arrow.isDead || !arrow.isValid) {
                    cancel()
                    return
                }

                val nearbyEntities = arrow.getNearbyEntities(4.0, 4.0, 4.0)
                for (entity in nearbyEntities) {
                    if (entity is LivingEntity && entity != player) {
                        entity.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 200, 1))
                        entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 200, 2))
                    }
                }
            }
        }.runTaskTimer(org.ReDiego0.combatSystem.CombatSystem.instance, 0L, 2L)
    }
}
