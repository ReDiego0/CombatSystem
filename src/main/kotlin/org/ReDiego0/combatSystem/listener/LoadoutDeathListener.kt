package org.ReDiego0.combatSystem.listener

import org.ReDiego0.combatSystem.loadout.LoadoutEquipper
import org.ReDiego0.combatSystem.loadout.LoadoutManager
import org.ReDiego0.combatSystem.world.TownyIntegration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin

class LoadoutDeathListener(
    private val plugin: JavaPlugin,
    private val loadoutEquipper: LoadoutEquipper,
    private val loadoutManager: LoadoutManager,
    private val townyIntegration: TownyIntegration
) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[LoadoutDeathListener] Registered successfully")
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity

        if (townyIntegration.isPlayerInCity(player)) return

        if (loadoutEquipper.isLoadoutEquipped(player)) {
            event.drops.clear()

            loadoutEquipper.saveLoadout(player)

            player.sendMessage("§a[Loadout] Your equipment has been safely stored in your loadout")

            if (plugin.config.getBoolean("debug", false)) {
                plugin.logger.info("[LoadoutDeathListener] ${player.name} died - loadout saved, drops cleared")
            }
        }
    }
}
