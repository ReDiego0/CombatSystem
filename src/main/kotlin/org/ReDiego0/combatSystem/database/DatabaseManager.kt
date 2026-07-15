package org.ReDiego0.combatSystem.database

import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class DatabaseManager(private val plugin: JavaPlugin) {

    private lateinit var connection: Connection

    fun init() {
        val dbFile = File(plugin.dataFolder, "data.db")
        plugin.dataFolder.mkdirs()

        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")

        createTables()
        plugin.logger.info("[DatabaseManager] Database initialized")
    }

    private fun createTables() {
        connection.prepareStatement("""
            CREATE TABLE IF NOT EXISTS loadouts (
                uuid TEXT PRIMARY KEY,
                weapon1 TEXT,
                weapon2 TEXT,
                helmet TEXT,
                chestplate TEXT,
                leggings TEXT,
                boots TEXT,
                support_items TEXT,
                is_equipped INTEGER DEFAULT 0
            )
        """).use { it.execute() }

        connection.prepareStatement("""
            CREATE TABLE IF NOT EXISTS resource_storage (
                uuid TEXT PRIMARY KEY,
                items TEXT
            )
        """).use { it.execute() }
    }

    fun getConnection(): Connection = connection

    fun close() {
        if (::connection.isInitialized && !connection.isClosed) {
            connection.close()
        }
    }

    fun executeUpdate(sql: String, vararg params: Any?) {
        connection.prepareStatement(sql).use { stmt ->
            params.forEachIndexed { i, param ->
                when (param) {
                    is String -> stmt.setString(i + 1, param)
                    is Int -> stmt.setInt(i + 1, param)
                    is Long -> stmt.setLong(i + 1, param)
                    is Double -> stmt.setDouble(i + 1, param)
                    is ByteArray -> stmt.setBytes(i + 1, param)
                    else -> stmt.setString(i + 1, param?.toString())
                }
            }
            stmt.executeUpdate()
        }
    }

    fun executeQuery(sql: String, vararg params: Any?, handler: (ResultSet) -> Unit) {
        connection.prepareStatement(sql).use { stmt ->
            params.forEachIndexed { i, param ->
                when (param) {
                    is String -> stmt.setString(i + 1, param)
                    is Int -> stmt.setInt(i + 1, param)
                    is Long -> stmt.setLong(i + 1, param)
                    is Double -> stmt.setDouble(i + 1, param)
                    is ByteArray -> stmt.setBytes(i + 1, param)
                    else -> stmt.setString(i + 1, param?.toString())
                }
            }
            stmt.executeQuery().use { rs ->
                handler(rs)
            }
        }
    }
}
