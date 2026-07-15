package org.ReDiego0.combatSystem.combat

import org.ReDiego0.combatSystem.core.PDCUtil
import org.ReDiego0.combatSystem.core.StaminaManager
import org.ReDiego0.combatSystem.data.Rarity
import org.ReDiego0.combatSystem.data.WeaponCategory
import org.ReDiego0.combatSystem.item.ItemFactory
import org.bukkit.entity.Player

class ActionbarHUD(
    private val staminaManager: StaminaManager,
    private val combatManager: CombatManager
) {

    fun update(player: Player) {
        val item = player.inventory.itemInMainHand
        if (!ItemFactory.isManagedWeapon(item)) return

        val categoryName = PDCUtil.getString(item, "category") ?: return
        val category = WeaponCategory.fromString(categoryName) ?: return
        val rarityName = PDCUtil.getString(item, "rarity") ?: return
        val rarity = Rarity.fromString(rarityName) ?: return

        val stamina = staminaManager.getStamina(player)
        val maxStamina = staminaManager.getMaxStamina()

        val skill1Name = combatManager.getSkill1Name(category)
        val skill1Cooldown = combatManager.getCooldownRemaining(player, "skill1_${category.name}")
        val skill1Status = if (skill1Cooldown > 0) "${"%.1f".format(skill1Cooldown)}s" else "Ready"

        val skill2Name = combatManager.getSkill2Name(category)
        val skill2Cooldown = combatManager.getCooldownRemaining(player, "skill2_${category.name}")
        val skill2Status = if (skill2Cooldown > 0) "${"%.1f".format(skill2Cooldown)}s" else "Ready"

        val actionBar = buildString {
            append("§e[Q: $skill1Name ($skill1Status)] ")
            append("⚡ §a${stamina.toInt()}/${maxStamina.toInt()} ⚡ ")
            append("§e[F: $skill2Name ($skill2Status)]")

            if (rarity == Rarity.MYTHIC) {
                val ultimateCooldown = combatManager.getCooldownRemaining(player, "ultimate")
                val ultimateStatus = if (ultimateCooldown > 0) "${"%.1f".format(ultimateCooldown)}s" else "Ready"
                append(" ⚡ §c[RMB: Ultimate ($ultimateStatus)]")
            }
        }

        player.sendActionBar(org.bukkit.ChatColor.translateAlternateColorCodes('&', actionBar))
    }
}
