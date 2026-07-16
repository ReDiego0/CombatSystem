package org.ReDiego0.combatSystem.api.event

import org.ReDiego0.combatSystem.data.LootPool
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class LootRollEvent(
    val player: Player,
    val mobType: EntityType,
    val pool: LootPool,
    val drop: LootPool.LootDrop?
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
