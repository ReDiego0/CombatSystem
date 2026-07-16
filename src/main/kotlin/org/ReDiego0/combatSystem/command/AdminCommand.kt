package org.ReDiego0.combatSystem.command

import org.ReDiego0.combatSystem.item.ArmorLoader
import org.ReDiego0.combatSystem.item.ItemFactory
import org.ReDiego0.combatSystem.item.WeaponRegistry
import org.ReDiego0.combatSystem.loadout.LoadoutManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class AdminCommand(
    private val plugin: JavaPlugin,
    private val weaponRegistry: WeaponRegistry,
    private val armorLoader: ArmorLoader,
    private val loadoutManager: LoadoutManager
) : CommandExecutor, TabCompleter {

    fun register() {
        plugin.getCommand("combatsystem")?.setExecutor(this)
        plugin.getCommand("combatsystem")?.setTabCompleter(this)
        plugin.logger.info("[AdminCommand] Registered successfully")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "reload" -> handleReload(sender)
            "giveweapon" -> handleGiveWeapon(sender, args)
            "givearmor" -> handleGiveArmor(sender, args)
            else -> sendHelp(sender)
        }

        return true
    }

    private fun handleReload(sender: CommandSender) {
        if (!sender.hasPermission("combatsystem.admin")) {
            sender.sendMessage("§c[CombatSystem] You don't have permission to do this.")
            return
        }

        plugin.server.pluginManager.getPlugin("CombatSystem")?.let {
            (it as org.ReDiego0.combatSystem.CombatSystem).reloadPlugin()
            sender.sendMessage("§a[CombatSystem] Configuration reloaded successfully!")
        }
    }

    private fun handleGiveWeapon(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("combatsystem.admin.give")) {
            sender.sendMessage("§c[CombatSystem] You don't have permission to do this.")
            return
        }

        if (args.size < 3) {
            sender.sendMessage("§c[CombatSystem] Usage: /cs giveweapon <player> <weapon_id> [tier] [level]")
            return
        }

        val playerName = args[1]
        val weaponId = args[2]
        val tier = if (args.size > 3) args[3].toIntOrNull() ?: 0 else 0
        val level = if (args.size > 4) args[4].toIntOrNull() ?: 0 else 0

        val player = plugin.server.getPlayer(playerName)
        if (player == null) {
            sender.sendMessage("§c[CombatSystem] Player '$playerName' not found.")
            return
        }

        val weaponData = weaponRegistry.get(weaponId)
        if (weaponData == null) {
            sender.sendMessage("§c[CombatSystem] Weapon '$weaponId' not found.")
            return
        }

        val item = ItemFactory.createWeapon(weaponData)

        loadoutManager.loadPlayerLoadout(player.uniqueId)
        val loadout = loadoutManager.getLoadout(player.uniqueId)

        if (loadout != null) {
            if (loadout.weapon1 == null) {
                loadoutManager.updateWeapon(player.uniqueId, 0, item)
            } else if (loadout.weapon2 == null) {
                loadoutManager.updateWeapon(player.uniqueId, 1, item)
            } else {
                sender.sendMessage("§c[CombatSystem] Player's loadout weapon slots are full.")
                return
            }
            sender.sendMessage("§a[CombatSystem] Weapon '$weaponId' given to ${player.name}")
            player.sendMessage("§a[CombatSystem] You received a new weapon!")
        } else {
            sender.sendMessage("§c[CombatSystem] Could not load player's loadout.")
        }
    }

    private fun handleGiveArmor(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("combatsystem.admin.give")) {
            sender.sendMessage("§c[CombatSystem] You don't have permission to do this.")
            return
        }

        if (args.size < 3) {
            sender.sendMessage("§c[CombatSystem] Usage: /cs givearmor <player> <armor_id> [tier] [level]")
            return
        }

        val playerName = args[1]
        val armorId = args[2]

        val player = plugin.server.getPlayer(playerName)
        if (player == null) {
            sender.sendMessage("§c[CombatSystem] Player '$playerName' not found.")
            return
        }

        val armorData = armorLoader.get(armorId)
        if (armorData == null) {
            sender.sendMessage("§c[CombatSystem] Armor '$armorId' not found.")
            return
        }

        sender.sendMessage("§a[CombatSystem] Armor '$armorId' given to ${player.name}")
        player.sendMessage("§a[CombatSystem] You received new armor!")
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("§6[CombatSystem] §fCommands:")
        sender.sendMessage("§7/cs reload §f- Reload configuration")
        sender.sendMessage("§7/cs giveweapon <player> <id> [tier] [level] §f- Give weapon")
        sender.sendMessage("§7/cs givearmor <player> <id> [tier] [level] §f- Give armor")
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1) {
            return listOf("reload", "giveweapon", "givearmor").filter { it.startsWith(args[0], ignoreCase = true) }
        }

        if (args.size == 2) {
            when (args[0].lowercase()) {
                "giveweapon", "givearmor" -> {
                    return plugin.server.onlinePlayers.map { it.name }.filter { it.startsWith(args[1], ignoreCase = true) }
                }
            }
        }

        if (args.size == 3) {
            when (args[0].lowercase()) {
                "giveweapon" -> {
                    return weaponRegistry.getAll().keys.filter { it.startsWith(args[2], ignoreCase = true) }
                }
                "givearmor" -> {
                    return armorLoader.getAll().keys.filter { it.startsWith(args[2], ignoreCase = true) }
                }
            }
        }

        return emptyList()
    }
}
