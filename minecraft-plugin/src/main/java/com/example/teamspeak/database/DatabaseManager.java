package com.example.teamspeak.database;

import com.example.teamspeak.TeamSpeakIntegration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final TeamSpeakIntegration plugin;
    private HikariDataSource dataSource;
    private final String databaseType;

    public DatabaseManager(TeamSpeakIntegration plugin) {
        this.plugin = plugin;
        this.databaseType = plugin.getConfigManager().getDatabaseType();
        initializeDataSource();
    }

    private void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        
        if ("postgres".equalsIgnoreCase(databaseType)) {
            // PostgreSQL configuration
            config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s",
                    plugin.getConfigManager().getPostgresHost(),
                    plugin.getConfigManager().getPostgresPort(),
                    plugin.getConfigManager().getPostgresDatabase()));
            config.setUsername(plugin.getConfigManager().getPostgresUsername());
            config.setPassword(plugin.getConfigManager().getPostgresPassword());
            config.setMaximumPoolSize(plugin.getConfigManager().getPostgresPoolSize());
            config.setConnectionTimeout(plugin.getConfigManager().getPostgresConnectionTimeout());
        } else {
            // SQLite configuration
            String dbPath = plugin.getConfigManager().getSqliteFile();
            File dbFile = new File(plugin.getDataFolder().getParentFile(), dbPath);
            
            // Ensure parent directory exists
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setMaximumPoolSize(plugin.getConfigManager().getSqlitePoolSize());
            config.setConnectionTimeout(plugin.getConfigManager().getSqliteConnectionTimeout());
            
            // SQLite-specific settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        }

        dataSource = new HikariDataSource(config);
        
        // Initialize database schema
        initializeSchema();
    }

    private void initializeSchema() {
        try (Connection conn = dataSource.getConnection()) {
            // Create tables if they don't exist
            createTables(conn);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error initializing database schema: " + e.getMessage());
        }
    }

    private void createTables(Connection conn) throws SQLException {
        // Table for storing linked accounts
        String createLinkedAccountsTable = "CREATE TABLE IF NOT EXISTS linked_accounts (" +
                "id INTEGER PRIMARY KEY " + getAutoIncrementSyntax() + ", " +
                "minecraft_uuid VARCHAR(36) NOT NULL UNIQUE, " +
                "minecraft_username VARCHAR(16) NOT NULL, " +
                "teamspeak_uid VARCHAR(28) NOT NULL UNIQUE, " +
                "teamspeak_username VARCHAR(64) NOT NULL, " +
                "linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_verified TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        
        // Table for caching TeamSpeak users
        String createTeamSpeakUsersTable = "CREATE TABLE IF NOT EXISTS teamspeak_users_cache (" +
                "id INTEGER PRIMARY KEY " + getAutoIncrementSyntax() + ", " +
                "teamspeak_uid VARCHAR(28) NOT NULL UNIQUE, " +
                "username VARCHAR(64) NOT NULL, " +
                "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_online BOOLEAN DEFAULT 0, " +
                "server_groups TEXT)";
        
        // Table for storing role mappings
        String createRoleMappingsTable = "CREATE TABLE IF NOT EXISTS role_mappings (" +
                "id INTEGER PRIMARY KEY " + getAutoIncrementSyntax() + ", " +
                "minecraft_rank VARCHAR(32) NOT NULL, " +
                "teamspeak_group_id INTEGER NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        
        // Create indexes
        String createLinkedAccountsIndexes = 
                "CREATE INDEX IF NOT EXISTS idx_linked_accounts_minecraft_uuid ON linked_accounts(minecraft_uuid); " +
                "CREATE INDEX IF NOT EXISTS idx_linked_accounts_teamspeak_uid ON linked_accounts(teamspeak_uid);";
        
        String createTeamSpeakUsersIndexes = 
                "CREATE INDEX IF NOT EXISTS idx_teamspeak_users_cache_uid ON teamspeak_users_cache(teamspeak_uid);";
        
        String createRoleMappingsIndexes = 
                "CREATE INDEX IF NOT EXISTS idx_role_mappings_minecraft_rank ON role_mappings(minecraft_rank);";
        
        // Execute all SQL statements
        try (PreparedStatement stmt = conn.prepareStatement(createLinkedAccountsTable)) {
            stmt.execute();
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(createTeamSpeakUsersTable)) {
            stmt.execute();
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(createRoleMappingsTable)) {
            stmt.execute();
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(createLinkedAccountsIndexes)) {
            stmt.execute();
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(createTeamSpeakUsersIndexes)) {
            stmt.execute();
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(createRoleMappingsIndexes)) {
            stmt.execute();
        }
    }
    
    private String getAutoIncrementSyntax() {
        return "postgres".equalsIgnoreCase(databaseType) ? "SERIAL" : "AUTOINCREMENT";
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Gets the HikariDataSource instance
     * @return The HikariDataSource instance
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    // Account Linking Methods
    public CompletableFuture<Boolean> linkAccounts(UUID minecraftUuid, String minecraftUsername, String teamspeakUid, String teamspeakUsername) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO linked_accounts (minecraft_uuid, minecraft_username, teamspeak_uid, teamspeak_username) " +
                        "VALUES (?, ?, ?, ?)";
            
            if ("postgres".equalsIgnoreCase(databaseType)) {
                sql += " ON CONFLICT (minecraft_uuid) DO UPDATE SET " +
                       "teamspeak_uid = EXCLUDED.teamspeak_uid, teamspeak_username = EXCLUDED.teamspeak_username, " +
                       "last_verified = CURRENT_TIMESTAMP";
            } else {
                sql += " ON CONFLICT(minecraft_uuid) DO UPDATE SET " +
                       "teamspeak_uid = excluded.teamspeak_uid, teamspeak_username = excluded.teamspeak_username, " +
                       "last_verified = CURRENT_TIMESTAMP";
            }
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, minecraftUuid.toString());
                stmt.setString(2, minecraftUsername);
                stmt.setString(3, teamspeakUid);
                stmt.setString(4, teamspeakUsername);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Error linking accounts: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> unlinkAccount(UUID minecraftUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM linked_accounts WHERE minecraft_uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, minecraftUuid.toString());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Error unlinking account: " + e.getMessage());
                return false;
            }
        });
    }

    // TeamSpeak User Cache Methods
    public CompletableFuture<Void> updateTeamSpeakUser(String teamspeakUid, String username, boolean isOnline, String[] serverGroups) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO teamspeak_users_cache (teamspeak_uid, username, is_online, server_groups) " +
                        "VALUES (?, ?, ?, ?)";
            
            if ("postgres".equalsIgnoreCase(databaseType)) {
                sql += " ON CONFLICT (teamspeak_uid) DO UPDATE SET " +
                       "username = EXCLUDED.username, is_online = EXCLUDED.is_online, " +
                       "server_groups = EXCLUDED.server_groups, last_seen = CURRENT_TIMESTAMP";
            } else {
                sql += " ON CONFLICT(teamspeak_uid) DO UPDATE SET " +
                       "username = excluded.username, is_online = excluded.is_online, " +
                       "server_groups = excluded.server_groups, last_seen = CURRENT_TIMESTAMP";
            }
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, teamspeakUid);
                stmt.setString(2, username);
                stmt.setBoolean(3, isOnline);
                
                // Handle server groups differently for PostgreSQL and SQLite
                if ("postgres".equalsIgnoreCase(databaseType)) {
                    stmt.setArray(4, conn.createArrayOf("text", serverGroups));
                } else {
                    // For SQLite, store as comma-separated string
                    stmt.setString(4, String.join(",", serverGroups));
                }
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error updating TeamSpeak user cache: " + e.getMessage());
            }
        });
    }

    // Role Mapping Methods
    public CompletableFuture<Void> updateRoleMapping(String minecraftRank, int teamspeakGroupId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO role_mappings (minecraft_rank, teamspeak_group_id) " +
                        "VALUES (?, ?)";
            
            if ("postgres".equalsIgnoreCase(databaseType)) {
                sql += " ON CONFLICT (minecraft_rank) DO UPDATE SET " +
                       "teamspeak_group_id = EXCLUDED.teamspeak_group_id";
            } else {
                sql += " ON CONFLICT(minecraft_rank) DO UPDATE SET " +
                       "teamspeak_group_id = excluded.teamspeak_group_id";
            }
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, minecraftRank);
                stmt.setInt(2, teamspeakGroupId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error updating role mapping: " + e.getMessage());
            }
        });
    }

    // Query Methods
    public CompletableFuture<Boolean> isAccountLinked(UUID minecraftUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT EXISTS(SELECT 1 FROM linked_accounts WHERE minecraft_uuid = ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, minecraftUuid.toString());
                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getBoolean(1);
            } catch (SQLException e) {
                plugin.getLogger().severe("Error checking account link status: " + e.getMessage());
                return false;
            }
        });
    }
} 
