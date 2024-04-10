package me.arial.wintop.api.database.sqlite

import me.arial.wintop.api.database.WinDao
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface WinDaoSQLite : WinDao {

    @SqlQuery("select player from win")
    override fun getAllPlayers(): Collection<String?>?

    @SqlQuery(
        "SELECT player FROM win WHERE player = :ID"
    )
    override fun getPlayer(@Bind("ID") player: String): String?

    @SqlUpdate(
        "insert into win values(:ID, 0) ON CONFLICT(player) DO UPDATE SET player = :ID"
    )
    override fun addPlayer(@Bind("ID") player: String)

    @SqlUpdate(
        "DELETE FROM win WHERE player = :ID"
    )
    override fun removePlayer(@Bind("ID") player: String)

    @SqlQuery(
        "SELECT balance FROM win WHERE player = :ID"
    )
    override fun getBalance(@Bind("ID") player: String): Long?

    @SqlUpdate(
        "update win set balance = :BALANCE where player = :ID"
    )
    override fun updateBalance(@Bind("ID") player: String, @Bind("BALANCE") newBalance: Long)

    @SqlUpdate(
        "INSERT INTO `limit` (player, time, balance) \n" +
                "VALUES (:ID, :TIME, :BALANCE) \n" +
                "ON CONFLICT (player) DO UPDATE \n" +
                "SET time = EXCLUDED.time, \n" +
                "    balance = EXCLUDED.balance;\n"
    )
    override fun setLimit(@Bind("ID") player: String, @Bind("TIME") time: Long, @Bind("BALANCE") balance: Long)

    @SqlQuery(
        "SELECT `time` FROM `limit` WHERE player = :ID"
    )
    override fun getLimit(@Bind("ID") player: String): Long?

    @SqlQuery(
        "SELECT balance FROM `limit` WHERE player = :ID"
    )
    override fun getLimitBalance(@Bind("ID") player: String): Long?
}