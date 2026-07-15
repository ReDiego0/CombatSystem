package org.ReDiego0.combatSystem.listener

import org.ReDiego0.combatSystem.combat.ActionbarHUD
import org.ReDiego0.combatSystem.combat.CombatManager
import org.ReDiego0.combatSystem.combat.UltimateExecutor
import org.ReDiego0.combatSystem.combat.skills.*
import org.ReDiego0.combatSystem.config.ConfigManager
import org.ReDiego0.combatSystem.core.PDCUtil
import org.ReDiego0.combatSystem.core.StaminaManager
import org.ReDiego0.combatSystem.data.WeaponCategory
import org.ReDiego0.combatSystem.item.ItemFactory
import org.ReDiego0.combatSystem.world.TownyIntegration
import org.ReDiego0.combatSystem.world.WorldIsolation
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.plugin.java.JavaPlugin

class CombatInputListener(
    private val plugin: JavaPlugin,
    private val worldIsolation: WorldIsolation,
    private val configManager: ConfigManager,
    private val staminaManager: StaminaManager,
    private val combatManager: CombatManager,
    private val actionbarHUD: ActionbarHUD,
    private val townyIntegration: TownyIntegration
) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[CombatInputListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val player = event.player

        if (!isValidPlayer(player)) return
        if (!worldIsolation.isWorldEnabled(player.world.name)) return
        if (!canUseAbilities(player)) return

        val item = event.itemDrop.itemStack
        if (!ItemFactory.isManagedWeapon(item)) return

        val categoryName = PDCUtil.getString(item, "category") ?: return
        val category = WeaponCategory.fromString(categoryName) ?: return

        event.isCancelled = true

        val skillId = "skill1_${category.name}"
        if (combatManager.isOnCooldown(player, skillId)) {
            val remaining = combatManager.getCooldownRemaining(player, skillId)
            player.sendMessage("§c[Skill] ${combatManager.getSkill1Name(category)} on cooldown: ${"%.1f".format(remaining)}s")
            return
        }

        val staminaCost = combatManager.getSkill1StaminaCost(category)
        if (!staminaManager.consumeStamina(player, staminaCost)) {
            player.sendMessage("§c[Skill] Not enough stamina!")
            return
        }

        executeSkill1(player, category)
        combatManager.setCooldown(player, skillId, combatManager.getSkill1Cooldown(category))

        if (configManager.isDebug()) {
            plugin.logger.info("[CombatInputListener] ${player.name} used Skill 1: ${combatManager.getSkill1Name(category)}")
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerSwapHand(event: PlayerSwapHandItemsEvent) {
        val player = event.player

        if (!isValidPlayer(player)) return
        if (!worldIsolation.isWorldEnabled(player.world.name)) return
        if (!canUseAbilities(player)) return

        val item = player.inventory.itemInMainHand
        if (!ItemFactory.isManagedWeapon(item)) return

        val categoryName = PDCUtil.getString(item, "category") ?: return
        val category = WeaponCategory.fromString(categoryName) ?: return

        event.isCancelled = true

        val skillId = "skill2_${category.name}"
        if (combatManager.isOnCooldown(player, skillId)) {
            val remaining = combatManager.getCooldownRemaining(player, skillId)
            player.sendMessage("§c[Skill] ${combatManager.getSkill2Name(category)} on cooldown: ${"%.1f".format(remaining)}s")
            return
        }

        val staminaCost = combatManager.getSkill2StaminaCost(category)
        if (!staminaManager.consumeStamina(player, staminaCost)) {
            player.sendMessage("§c[Skill] Not enough stamina!")
            return
        }

        executeSkill2(player, category)
        combatManager.setCooldown(player, skillId, combatManager.getSkill2Cooldown(category))

        if (configManager.isDebug()) {
            plugin.logger.info("[CombatInputListener] ${player.name} used Skill 2: ${combatManager.getSkill2Name(category)}")
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (!isValidPlayer(player)) return
        if (!worldIsolation.isWorldEnabled(player.world.name)) return
        if (!canUseAbilities(player)) return

        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val item = player.inventory.itemInMainHand
        if (!ItemFactory.isManagedWeapon(item)) return

        if (!UltimateExecutor.canUseUltimate(player)) return

        event.isCancelled = true

        val skillId = "ultimate"
        if (combatManager.isOnCooldown(player, skillId)) {
            val remaining = combatManager.getCooldownRemaining(player, skillId)
            player.sendMessage("§c[Ultimate] On cooldown: ${"%.1f".format(remaining)}s")
            return
        }

        if (!staminaManager.consumeStamina(player, 50.0)) {
            player.sendMessage("§c[Ultimate] Not enough stamina!")
            return
        }

        UltimateExecutor.executeUltimate(player)
        combatManager.setCooldown(player, skillId, 30.0)

        if (configManager.isDebug()) {
            plugin.logger.info("[CombatInputListener] ${player.name} used Ultimate")
        }
    }

    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        if (!isValidPlayer(player)) return

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            actionbarHUD.update(player)
        }, 1L)
    }

    private fun canUseAbilities(player: Player): Boolean {
        return townyIntegration.canUseCombatAbilities(player)
    }

    private fun executeSkill1(player: Player, category: WeaponCategory) {
        when (category) {
            WeaponCategory.KATANA -> KatanaSkills.executeSwiftSlash(player)
            WeaponCategory.LONGSWORD -> LongswordSkills.executeLunge(player)
            WeaponCategory.HEAVY_HAMMER -> HammerSkills.executeGroundSlam(player)
            WeaponCategory.CROSSBOW -> CrossbowSkills.executeMultiShot(player)
            WeaponCategory.SPEAR -> SpearSkills.executeImpale(player)
            WeaponCategory.STAFF -> StaffSkills.executeArcaneBurst(player)
            WeaponCategory.BOW -> BowSkills.executeRapidShot(player)
        }
    }

    private fun executeSkill2(player: Player, category: WeaponCategory) {
        when (category) {
            WeaponCategory.KATANA -> KatanaSkills.executeParry(player)
            WeaponCategory.LONGSWORD -> LongswordSkills.executeBladeGuard(player)
            WeaponCategory.HEAVY_HAMMER -> HammerSkills.executeIronWill(player)
            WeaponCategory.CROSSBOW -> CrossbowSkills.executeNetShot(player)
            WeaponCategory.SPEAR -> SpearSkills.executeSweep(player)
            WeaponCategory.STAFF -> StaffSkills.executeBarrier(player)
            WeaponCategory.BOW -> BowSkills.executeExplosiveArrow(player)
        }
    }

    private fun isValidPlayer(player: Player): Boolean {
        return player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE
    }
}
