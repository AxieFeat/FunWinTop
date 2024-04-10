package me.arial.wintop.core

import me.arial.wintop.core.util.Color
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration

class Config() {

    private var config: FileConfiguration? = null;

    companion object {
        private val content: MutableMap<String, Any> = mutableMapOf()

        fun getBoolean(key: String): Boolean {
            return (content[key] ?: true) as Boolean
        }

        fun getLong(key: String): Long {
            return content[key]!!.toString().toLong()
        }

        fun getString(key: String): String {
            return content[key]!!.toString()
        }

        fun getStringList(key: String): List<String> {
            return content[key]!! as MutableList<String>
        }

        fun getMessage(key: String): String {
            return content["messages.$key"]!!.toString()
        }

        fun getMessageList(key: String): List<String> {
            return content["messages.$key"]!! as MutableList<String>
        }
    }

    init {
        Main.instance!!.saveDefaultConfig()
        config = Main.instance!!.getConfig()

        load()
    }

    private fun load() {
        val sections = listOf("messages", "settings")

        sections.forEach {section ->
            config!!.getConfigurationSection(section)!!.getKeys(true).forEach {
                content["$section.$it"] = Color.parse(config!!.get("$section.$it")!!)
            }
        }
    }

    fun reload() {
        content.clear()

        Main.instance!!.reloadConfig()

        this.config = Main.instance!!.getConfig()

        load()
    }
}