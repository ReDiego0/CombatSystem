package org.ReDiego0.combatSystem.combat.skills

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object SpearSkills {

    fun executeImpale(player: Player) {
        val direction = player.location.direction.normalize()
        val dashVector = direction.multiply(6.0)
        player.velocity = dashVector

        object : org.bukkit.scheduler.BukkitRunnable() {
            var ticks = 0
            override fun run() {
                if (ticks >= 10 || !player.isOnline) {
                    cancel()
                    return
                }

                val nearbyEntities = player.getNearbyEntities(2.0, 2.0, 2.0)
                for (entity in nearbyEntities) {
                    if (entity is LivingEntity && entity != player) {
                        entity.damage(12.0, player)
                    }
                }

                ticks++
            }
        }.runTaskTimer(org.ReDiego0.combatSystem.CombatSystem.instance, 0L, 2L)
    }

    fun executeSweep(player: Player) {
        val direction = player.location.direction
        val nearbyEntities = player.getNearbyEntities(4.0, 4.0, 4.0)

        for (entity in nearbyEntities) {
            if (entity is LivingEntity && entity != player) {
                val entityDir = entity.location.toVector().subtract(player.location.toVector()).normalize()
                val dot = direction.dot(entityDir)

                if (dot > 0.5) {
                    entity.damage(10.0, player)

                    val pushDir = entityDir.clone().multiply(1.2).setY(0.3)
                    entity.velocity = pushDir
                }
            }
        }
    }
}
