package org.ReDiego0.combatSystem.combat.skills

import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object BowSkills {

    fun executeRapidShot(player: Player) {
        val direction = player.location.direction

        object : org.bukkit.scheduler.BukkitRunnable() {
            var shots = 0
            override fun run() {
                if (shots >= 5 || !player.isOnline) {
                    cancel()
                    return
                }

                val arrow = player.launchProjectile(Arrow::class.java, direction.multiply(2.5))
                arrow.damage = 3.0

                shots++
            }
        }.runTaskTimer(org.ReDiego0.combatSystem.CombatSystem.instance, 0L, 4L)
    }

    fun executeExplosiveArrow(player: Player) {
        val direction = player.location.direction.normalize()

        val arrow = player.launchProjectile(Arrow::class.java, direction.multiply(2.0))
        arrow.damage = 8.0

        object : org.bukkit.scheduler.BukkitRunnable() {
            override fun run() {
                if (arrow.isDead || !arrow.isValid) {
                    cancel()
                    return
                }

                if (arrow.isOnGround || arrow.isInBlock) {
                    val loc = arrow.location

                    val nearbyEntities = arrow.getNearbyEntities(3.0, 3.0, 3.0)
                    for (entity in nearbyEntities) {
                        if (entity is LivingEntity && entity != player) {
                            entity.damage(10.0, player)
                            entity.fireTicks = 60
                        }
                    }

                    arrow.world.createExplosion(loc, 0.0f, false, false)
                    arrow.remove()
                    cancel()
                }
            }
        }.runTaskTimer(org.ReDiego0.combatSystem.CombatSystem.instance, 0L, 1L)
    }
}
