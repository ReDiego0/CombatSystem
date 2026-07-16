package org.ReDiego0.combatSystem.api.event

import org.ReDiego0.combatSystem.combat.TacticalDash
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CombatDashEvent(
    val player: Player,
    val direction: TacticalDash.DashDirection
) : Event(), Cancellable {

    private var cancelled = false

    override fun getHandlers(): HandlerList = handlerList

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {
        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }
}
