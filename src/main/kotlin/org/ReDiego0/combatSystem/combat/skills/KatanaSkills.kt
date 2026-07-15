package org.ReDiego0.combatSystem.combat.skills

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

object KatanaSkills {

    fun executeSwiftSlash(player: Player) {
        val direction = player.location.direction.normalize()
        val dashVector = direction.multiply(3.0)
        player.velocity = dashVector

        val startLoc = player.location.clone()
        val endLoc = startLoc.clone().add(dashVector)

        val nearbyEntities = player.getNearbyEntities(3.0, 3.0, 3.0)
        for (entity in nearbyEntities) {
            if (entity is LivingEntity && entity != player) {
                val entityLoc = entity.location
                if (isInLine(startLoc, entityLoc, endLoc, 1.5)) {
                    entity.damage(8.0, player)
                }
            }
        }
    }

    fun executeParry(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 30, 4))

        val nearbyEntities = player.getNearbyEntities(3.0, 3.0, 3.0)
        for (entity in nearbyEntities) {
            if (entity is LivingEntity && entity != player) {
                entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 127))
                entity.damage(12.0, player)
            }
        }
    }

    private fun isInLine(start: org.bukkit.Location, point: org.bukkit.Location, end: org.bukkit.Location, tolerance: Double): Boolean {
        val startVec = start.toVector()
        val pointVec = point.toVector()
        val endVec = end.toVector()

        val lineDir = endVec.clone().subtract(startVec)
        val pointDir = pointVec.clone().subtract(startVec)

        val projection = pointDir.dot(lineDir) / lineDir.lengthSquared()
        if (projection < 0 || projection > 1) return false

        val closestPoint = startVec.clone().add(lineDir.clone().multiply(projection))
        return pointVec.distance(closestPoint) <= tolerance
    }
}
