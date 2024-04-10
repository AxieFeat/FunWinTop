package me.arial.wintop.api.database

interface WinDao {
    fun getAllPlayers(): Collection<String?>?
    fun addPlayer(player: String)
    fun getPlayer(player: String): String?
    fun removePlayer(player: String)
    fun updateBalance(player: String, newBalance: Long)
    fun getBalance(player: String): Long?
    //fun addLimit(player: String, time: Long, balance: Long)
    fun setLimit(player: String, time: Long, balance: Long)
    fun getLimit(player: String): Long?
    fun getLimitBalance(player: String): Long?

    companion object {
        val DAO_CLASS: DaoClass = DaoClass()
    }
}