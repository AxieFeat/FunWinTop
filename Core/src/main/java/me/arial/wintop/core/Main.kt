package me.arial.wintop.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.arial.wintop.api.database.ConnectionInfo
import me.arial.wintop.api.database.WinDao
import me.arial.wintop.api.database.mysql.WinDaoMySQL
import me.arial.wintop.api.database.sqlite.WinDaoSQLite
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin
import org.jdbi.v3.cache.caffeine.CaffeineCachePlugin
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.HandleConsumer
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.async.JdbiExecutor
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Main : JavaPlugin() {

    companion object {
        var instance: Main? = null
    }

    private var jdbi: Jdbi? = null
    var executor: JdbiExecutor? = null
    var config: Config? = null
    var economy: Economy? = null

    override fun onEnable() {
        instance = this

        if (!setupEconomy()) {
            logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", description.name))
            server.pluginManager.disablePlugin(this)
            return
        }

        config = Config()

        initDB(connectionInfo)

        server.pluginManager.registerEvents(EventListener(), this)
        getCommand("win")!!.setExecutor(WinCommand())
        getCommand("win")!!.tabCompleter = WinCommand()
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }

        val rsp = server.servicesManager.getRegistration(
            Economy::class.java
        )
        if (rsp == null) {
            return false
        }

        economy = rsp.provider
        return true
    }

    private fun initDB(info: ConnectionInfo) {
        if (info.isMySQL) {
            initMySQL(info)
            WinDao.DAO_CLASS.clazz = WinDaoMySQL::class.java
        } else {
            initSQLite()
            WinDao.DAO_CLASS.clazz = WinDaoSQLite::class.java
        }
    }

    private fun initMySQL(info: ConnectionInfo) {
        val config = HikariConfig()

        config.jdbcUrl = "jdbc:mysql://" + info.host +
                ":" + info.port +
                "/" + info.database + "?autoReconnect=true&useSSL=false&characterEncoding=utf8"
        config.username = info.user
        config.password = info.password

        val dataSource = HikariDataSource(config)

        jdbi = Jdbi.create(dataSource)
        jdbi!!.installPlugin(CaffeineCachePlugin())
        jdbi!!.installPlugin(SqlObjectPlugin())

        try {
            this.javaClass.getResourceAsStream("/template.sql").use { inputStream ->
                jdbi!!.useHandle(HandleConsumer<IOException> { handle: Handle ->
                    handle.createScript(
                        String(inputStream!!.readAllBytes())
                    ).execute()
                })
                jdbi!!.useHandle(HandleConsumer<RuntimeException> { handle: Handle ->
                    handle.createUpdate(
                        "SET @@group_concat_max_len = 1000000"
                    ).execute()
                })
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        val executor: Executor = Executors.newFixedThreadPool(8)
        this.executor = JdbiExecutor.create(jdbi, executor)
    }

    private fun initSQLite() {
        val db = File(dataFolder, "database.db")
        if (!db.exists()) {
            try {
                db.createNewFile()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        val config = HikariConfig()
        config.driverClassName = "org.sqlite.JDBC"
        config.connectionTestQuery = "SELECT 1"
        config.jdbcUrl = "jdbc:sqlite:$db"

        val dataSource = HikariDataSource(config)

        jdbi = Jdbi.create(dataSource)
        jdbi!!.installPlugin(CaffeineCachePlugin())
        jdbi!!.installPlugin(SqlObjectPlugin())

        try {
            this.javaClass.getResourceAsStream("/template.sql").use { inputStream ->
                jdbi!!.useHandle(
                    HandleConsumer<IOException> { handle: Handle ->
                        handle.createScript(
                            String(
                                inputStream!!.readAllBytes()
                            )
                        ).execute()
                    })
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        val executor: Executor = Executors.newFixedThreadPool(8)
        this.executor = JdbiExecutor.create(jdbi, executor)
    }

    private val connectionInfo: ConnectionInfo
        get() {
            val isMySQL = getConfig().getBoolean("database.isMySQL")
            val host = getConfig().getString("database.host")!!
            val port = getConfig().getInt("database.port")
            val database = getConfig().getString("database.database")!!
            val user = getConfig().getString("database.user")!!
            val password = getConfig().getString("database.password")!!

            return ConnectionInfo(isMySQL, host, port, database, user, password)
        }
}