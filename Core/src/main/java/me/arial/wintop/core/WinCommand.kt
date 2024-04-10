package me.arial.wintop.core

import me.arial.wintop.api.database.WinDao
import me.arial.wintop.core.util.Extension.Companion.sendMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.LinkedHashMap

class WinCommand : TabExecutor {

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        str: String,
        args: Array<out String>
    ): MutableList<String> {

        val complete: MutableList<String> = mutableListOf()

        when(args.size) {
            1 -> {
                if (sender.hasPermission("win.reload")) complete.add("reload")
                if (sender.hasPermission("win.help")) complete.add("help")
                if (sender.hasPermission("win.top")) complete.add("top")
                if (sender.hasPermission("win.money")) complete.add("money")
                if (sender.hasPermission("win.invest")) complete.add("invest")
                if (sender.hasPermission("win.withdraw")) complete.add("withdraw")
                if (sender.hasPermission("win.info")) complete.add("info")
                if (sender.hasPermission("win.rules")) complete.add("rules")
                if (sender.hasPermission("win.prize")) complete.add("prize")
            }
        }

        return complete
    }

    override fun onCommand(sender: CommandSender, command: Command, str: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Config.getMessageList("onlyPlayer"))
            return true
        }

        val player: Player = sender

        if (args.isEmpty()) {
            sendUsage(player)
            return true
        }

        when(args[0]) {
            "reload" -> reloadHandle(player, args)
            "top" -> topHandle(player, args)
            "money" -> moneyHandle(player, args)
            "invest" -> investHandler(player, args)
            "withdraw" -> withdrawHandler(player, args)
            "info" -> infoHandler(player, args)
            "rules" -> rulesHandler(player, args)
            "prize" -> prizeHandler(player, args)

            else -> sendUsage(player)
        }

        return true
    }

    private fun sendUsage(player: Player) {
        if (!player.hasPermission("win.help")) {
            player.sendMessage(Config.getMessageList("winHelp.perm"))
            return
        }

        player.sendMessage(Config.getMessageList("winHelp.success"))
    }

    private fun reloadHandle(player: Player, args: Array<out String>) {
        if (!player.hasPermission("win.reload")) {
            player.sendMessage(Config.getMessageList("winReload.perm"))
            return
        }

        if (args.size != 1) {
            player.sendMessage(Config.getMessageList("winReload.usage"))
            return
        }

        Main.instance!!.config!!.reload()
        player.sendMessage(Config.getMessageList("winReload.success"))
    }

    private fun topHandle(player: Player, args: Array<out String>) {
        if (!player.hasPermission("win.top")) {
            player.sendMessage(Config.getMessageList("winTop.perm"))
            return
        }

        if (args.size != 1) {
            sendUsage(player)
            return
        }

        if (!Config.getBoolean("settings.features.winTop") && !player.hasPermission("win.bypass")) {
            player.sendMessage(Config.getMessageList("winTop.disabled"))
            return
        }

        Main.instance!!.executor!!.useExtension<_, Exception>(WinDao.DAO_CLASS.clazz) { dao ->
            val list = Config.getMessageList("winTop.success").toMutableList()
            val topLines = Config.getMessageList("winTop.topLines").toMutableList()

            val allPlayers = dao.getAllPlayers()?.toMutableList() ?: mutableListOf()
            val playersMap: MutableMap<String, Long> = mutableMapOf()

            allPlayers.forEach {
                playersMap[it!!] = dao.getBalance(it)!!
            }

            val defaultName = Config.getMessage("winTop.defaultName")
            val defaultBalance = Config.getMessage("winTop.defaultBalance")

            val sortedMap = playersMap.toList().sortedByDescending { (_, value) -> value }

            val decimalFormat = DecimalFormat(Config.getMessage("winTop.decimalFormat"), DecimalFormatSymbols.getInstance(Locale.ENGLISH))

            for (str in list.toList()) {
                if (str == "%top%") {
                    list.remove(str)

                    for (line in topLines) {
                        var pair = sortedMap.getOrNull(topLines.indexOf(line)) ?: Pair(defaultName, defaultBalance)

                        if (pair.second.toString() == "0") {
                            pair = Pair(defaultName, defaultBalance)
                        }

                        list.add(line
                            .replace(
                                "%player%",
                                pair.first
                            )
                            .replace(
                                "%balance%",
                                if (pair.second.toString() != defaultBalance) decimalFormat.format(pair.second.toString().toLong()) else pair.second.toString()
                            )
                        )

                    }

                }
            }

            player.sendMessage(list)
        }.exceptionally { ex: Throwable ->
            ex.printStackTrace()
            null
        }
    }

    private fun moneyHandle(player: Player, args: Array<out String>) {
        if (!player.hasPermission("win.money")) {
            player.sendMessage(Config.getMessageList("winMoney.perm"))
            return
        }

        if (args.size != 1) {
            sendUsage(player)
            return
        }

        if (!Config.getBoolean("settings.features.winMoney") && !player.hasPermission("win.bypass")) {
            player.sendMessage(Config.getMessageList("winMoney.disabled"))
            return
        }

        val decimalFormat = DecimalFormat(Config.getMessage("winMoney.decimalFormat"), DecimalFormatSymbols.getInstance(Locale.ENGLISH))

        Main.instance!!.executor!!.useExtension<_, Exception>(WinDao.DAO_CLASS.clazz) { dao ->
            player.sendMessage(
                Config.getMessageList("winMoney.success").stream()
                    .map {
                        it.replace(
                            "%balance%",
                            decimalFormat.format(dao.getBalance(player.name))
                        )
                    }.toList()
            )

            return@useExtension
        }.exceptionally { ex: Throwable ->
            ex.printStackTrace()
            null
        }
    }

    private fun investHandler(player: Player, args: Array<out String>) {
        if (!player.hasPermission("win.invest")) {
            player.sendMessage(Config.getMessageList("winInvest.perm"))
            return
        }

        if (args.size != 2) {
            sendUsage(player)
            return
        }

        if (!Config.getBoolean("settings.features.winInvest") && !player.hasPermission("win.bypass")) {
            player.sendMessage(Config.getMessageList("winInvest.disabled"))
            return
        }

        val number: Long = args[1].toLongOrNull() ?: -1

        if (number < 1) {
            player.sendMessage(Config.getMessageList("winInvest.numberError"))
            return
        }

        if (Main.instance!!.economy!!.getBalance(player) < number) {
            player.sendMessage(Config.getMessageList("winInvest.balanceError"))
            return
        }

        if (Config.getBoolean("settings.limit.enabled") && !player.hasPermission("win.bypass")) {

            Main.instance!!.executor!!.useExtension<_, Exception>(WinDao.DAO_CLASS.clazz) { dao ->
                var time = dao.getLimit(player.name)!!

                if (System.currentTimeMillis() >= time) {
                    time = System.currentTimeMillis() + Config.getLong("settings.limit.time")

                    dao.setLimit(
                        player.name,
                        time,
                        0L
                    )
                }

                val balance = dao.getLimitBalance(player.name)!!

                if ((balance + number) > Config.getLong("settings.limit.amount")) {
                    player.sendMessage(Config.getMessageList("winInvest.limit"))
                    return@useExtension
                }

                Main.instance!!.economy!!.withdrawPlayer(player, number.toDouble())

                dao.setLimit(
                    player.name,
                    time,
                    balance + number
                )

                dao.updateBalance(
                    player.name,
                    dao.getBalance(player.name)!! + number
                )

                player.sendMessage(Config.getMessageList("winInvest.success"))
                return@useExtension
            }.exceptionally { ex: Throwable ->
                ex.printStackTrace()
                null
            }

            return
        }

        Main.instance!!.executor!!.useExtension<_, Exception>(WinDao.DAO_CLASS.clazz) { dao ->

            Main.instance!!.economy!!.withdrawPlayer(player, number.toDouble())

            dao.updateBalance(
                player.name,
                dao.getBalance(player.name)!! + number
            )

            player.sendMessage(Config.getMessageList("winInvest.success"))
        }.exceptionally { ex: Throwable ->
            ex.printStackTrace()
            null
        }
    }

    private fun withdrawHandler(player: Player, args: Array<out String>) {
        if (!player.hasPermission("win.withdraw")) {
            player.sendMessage(Config.getMessageList("winWithdraw.perm"))
            return
        }

        if (args.size != 2) {
            sendUsage(player)
            return
        }

        if (!Config.getBoolean("settings.features.winWithdraw") && !player.hasPermission("win.bypass")) {
            player.sendMessage(Config.getMessageList("winWithdraw.disabled"))
            return
        }

        val number: Long = args[1].toLongOrNull() ?: 0

        if (number < 1) {
            player.sendMessage(Config.getMessageList("winWithdraw.numberError"))
            return
        }

        Main.instance!!.executor!!.useExtension<_, Exception>(WinDao.DAO_CLASS.clazz) { dao ->
            val currentBalance = dao.getBalance(player.name)

            if (number > currentBalance!!) {
                player.sendMessage(Config.getMessageList("winWithdraw.amountError"))
                return@useExtension
            }

            val newBalance = currentBalance - number

            dao.updateBalance(
                player.name,
                newBalance
            )

            Main.instance!!.economy!!.depositPlayer(player, number.toDouble())
            player.sendMessage(Config.getMessageList("winWithdraw.success"))

            return@useExtension
        }.exceptionally { ex: Throwable ->
            ex.printStackTrace()
            null
        }
    }

    private fun infoHandler(player: Player, args: Array<out String>) {
        if (!player.hasPermission("win.info")) {
            player.sendMessage(Config.getMessageList("winInfo.perm"))
            return
        }

        if (args.size != 1) {
            sendUsage(player)
            return
        }

        player.sendMessage(Config.getMessageList("winInfo.success"))
    }

    private fun rulesHandler(player: Player, args: Array<out String>) {
        if (!player.hasPermission("win.rules")) {
            player.sendMessage(Config.getMessageList("winRules.perm"))
            return
        }

        if (args.size != 1) {
            sendUsage(player)
            return
        }

        player.sendMessage(Config.getMessageList("winRules.success"))
    }

    private fun prizeHandler(player: Player, args: Array<out String>) {
        if (!player.hasPermission("win.prize")) {
            player.sendMessage(Config.getMessageList("winPrize.perm"))
            return
        }

        if (args.size != 1) {
            sendUsage(player)
            return
        }

        player.sendMessage(Config.getMessageList("winPrize.success"))
    }
}