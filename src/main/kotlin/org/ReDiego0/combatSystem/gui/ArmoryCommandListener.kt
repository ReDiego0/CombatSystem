package org.ReDiego0.combatSystem.gui

import org.ReDiego0.combatSystem.item.ItemFactory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class ArmoryCommandListener(
    private val plugin: JavaPlugin,
    private val armoryListener: ArmoryListener
) : CommandExecutor {

    fun register() {
        plugin.getCommand("arma")?.setExecutor(this)
        plugin.logger.info("[ArmoryCommandListener] Registered successfully")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§c[Armory] This command can only be used by players.")
            return true
        }

        if (!sender.hasPermission("combatsystem.armory")) {
            sender.sendMessage("§c[Armory] You don't have permission to use this command.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("§6[Armory] §fUsage: /arma modificar")
            return true
        }

        when (args[0].lowercase()) {
            "modificar" -> {
                val item = sender.inventory.itemInMainHand
                if (!ItemFactory.isManagedWeapon(item)) {
                    sender.sendMessage("§c[Armory] You must hold a managed weapon.")
                    return true
                }

                armoryListener.openArmory(sender, item)
            }
            else -> {
                sender.sendMessage("§6[Armory] §fUsage: /arma modificar")
            }
        }

        return true
    }
}
