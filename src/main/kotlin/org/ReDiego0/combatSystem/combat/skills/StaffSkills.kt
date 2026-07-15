package org.ReDiego0.combatSystem.combat.skills

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object StaffSkills {

    fun executeArcaneBurst(player: Player) {
        val direction = player.location.direction.normalize()

        val projectile = player.launchProjectile(Snowball::class.java, direction.multiply(2.0))

        object : org.bukkit.scheduler.BukkitRunnable() {
            var ticks = 0
            override fun run() {
                if (ticks >= 50 || projectile.isDead || !projectile.isValid) {
                    cancel()
                    projectile.remove()
                    return
                }

                val nearbyEntities = projectile.getNearbyEntities(1.0, 1.0, 1.0)
                for (entity in nearbyEntities) {
                    if (entity is LivingEntity && entity != player) {
                        entity.damage(6.0, player)
                    }
                }

                ticks++
            }
        }.runTaskTimer(org.ReDiego0.combatSystem.CombatSystem.instance, 0L, 1L)
    }

    fun executeBarrier(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 200, 2))

        object : org.bukkit.scheduler.BukkitRunnable() {
            var ticks = 0
            override fun run() {
                if (ticks >= 200 || !player.isOnline) {
                    cancel()
                    return
                }

                val nearbyEntities = player.getNearbyEntities(3.0, 3.0, 3.0)
                for (entity in nearbyEntities) {
                    if (entity is org.bukkit.entity.Projectile) {
                        val shooter = entity.shooter
                        if (shooter is LivingEntity && shooter != player) {
                            entity.remove()
                        }
                    }
                }

                ticks += 10
            }
        }.runTaskTimer(org.ReDiego0.combatSystem.CombatSystem.instance, 0L, 10L)
    }
}
