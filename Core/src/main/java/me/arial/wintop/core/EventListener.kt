package me.arial.wintop.core

import me.arial.wintop.api.database.WinDao
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class EventListener : Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        Main.instance!!.executor!!.useExtension<_, Exception>(WinDao.DAO_CLASS.clazz) { dao ->
            if (dao.getPlayer(player.name) == null) {
                dao.addPlayer(player.name)

                val time: Long = System.currentTimeMillis().plus(Config.getLong("settings.limit.time"))
                val balance = 0L

                dao.setLimit(
                    player.name,
                    time,
                    balance
                )
            }
        }.exceptionally {
            it.printStackTrace()
            null
        }
    }
}