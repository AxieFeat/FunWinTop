package me.arial.wintop.api.database.entity

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.beans.ConstructorProperties

data class PlayerEntry @ConstructorProperties(
    "player",
    "balance",
) constructor (
    val name: String,
    val balance: Long
) {
    val player: Player? = Bukkit.getPlayer(name)
    val offlinePlayer: OfflinePlayer = Bukkit.getOfflinePlayer(name)
}