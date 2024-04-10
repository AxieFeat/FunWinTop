package me.arial.wintop.api.database

@JvmRecord
data class ConnectionInfo(
    val isMySQL: Boolean,
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String
)