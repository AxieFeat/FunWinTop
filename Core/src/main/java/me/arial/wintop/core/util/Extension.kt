package me.arial.wintop.core.util

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Extension {
    companion object {
        fun Player.sendMessage(messages: List<String>) {
            messages.forEach {
                sendMessage(it)
            }
        }

        fun CommandSender.sendMessage(messages: List<String>) {
            messages.forEach {
                sendMessage(it)
            }
        }
    }
}