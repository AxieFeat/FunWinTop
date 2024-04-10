package me.arial.wintop.core.util

import net.md_5.bungee.api.ChatColor
import java.util.regex.Pattern

class Color {
    companion object {
        fun parseString(message: String): String {
            var message = message
            val pattern = Pattern.compile("&#[a-fA-F0-9]{6}")
            var matcher = pattern.matcher(message)

            while (matcher.find()) {
                val color = message.substring(matcher.start() + 1, matcher.end())
                message = message.replace("&$color", ChatColor.of(color).toString() + "")
                matcher = pattern.matcher(message)
            }

            return ChatColor.translateAlternateColorCodes('&', message)
        }

        fun parse(message: Any): Any {
            return when (message) {
                is String -> parseString(message)
                is MutableList<*> -> message.stream().map { parseString(it.toString()) }.toList()
                else -> message
            }
        }
    }
}