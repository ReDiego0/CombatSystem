package org.ReDiego0.combatSystem.combat.skills

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

object HammerSkills {

    fun executeGroundSlam(player: Player) {
        player.velocity = Vector(0.0, 1.5, 0.0)

        object : org.bukkit.scheduler.BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) return

                val landingLoc = player.location

                val nearbyEntities = player.getNearbyEntities(4.0, 4.0, 4.0)
                for (entity in nearbyEntities) {
                    if (entity is LivingEntity && entity != player) {
                        val distance = entity.location.distance(landingLoc)
                        if (distance <= 4.0) {
                            val damage = 22.0 * (1.0 - distance / 8.0)
                            entity.damage(damage.coerceAtLeast(5.0), player)

                            val knockback = entity.location.toVector()
                                .subtract(landingLoc.toVector())
                                .normalize()
                                .multiply(1.5)
                                .setY(0.5)
                            entity.velocity = knockback
                        }
                    }
                }
            }
        }.runTaskLater(org.ReDiego0.combatSystem.CombatSystem.instance, 15L)
    }

    fun executeIronWill(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 140, 2))
    }
}
