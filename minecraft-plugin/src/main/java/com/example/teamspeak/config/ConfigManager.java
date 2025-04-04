package com.example.teamspeak.config;

import com.example.teamspeak.TeamSpeakIntegration;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final TeamSpeakIntegration plugin;
    private FileConfiguration config;

    public ConfigManager(TeamSpeakIntegration plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // TeamSpeak Configuration
    public String getTeamSpeakHost() {
        return config.getString("teamspeak.host", "localhost");
    }

    public int getTeamSpeakPort() {
        return config.getInt("teamspeak.port", 10011);
    }

    public String getTeamSpeakUsername() {
        return config.getString("teamspeak.username", "serverquery");
    }

    public String getTeamSpeakPassword() {
        return config.getString("teamspeak.password", "");
    }

    public int getTeamSpeakVirtualServerId() {
        return config.getInt("teamspeak.virtual_server_id", 1);
    }

    public int getTeamSpeakUpdateInterval() {
        return config.getInt("teamspeak.update_interval", 30);
    }

    // Database Configuration
    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }

    // PostgreSQL Configuration
    public String getPostgresHost() {
        return config.getString("database.postgres.host", "localhost");
    }

    public int getPostgresPort() {
        return config.getInt("database.postgres.port", 5432);
    }

    public String getPostgresDatabase() {
        return config.getString("database.postgres.database", "minecraft_teamspeak");
    }

    public String getPostgresUsername() {
        return config.getString("database.postgres.username", "minecraft");
    }

    public String getPostgresPassword() {
        return config.getString("database.postgres.password", "minecraft");
    }

    public int getPostgresPoolSize() {
        return config.getInt("database.postgres.pool_size", 10);
    }

    public int getPostgresConnectionTimeout() {
        return config.getInt("database.postgres.connection_timeout", 30000);
    }

    // SQLite Configuration
    public String getSqliteFile() {
        return config.getString("database.sqlite.file", "plugins/TeamSpeakIntegration/database.db");
    }

    public int getSqlitePoolSize() {
        return config.getInt("database.sqlite.pool_size", 5);
    }

    public int getSqliteConnectionTimeout() {
        return config.getInt("database.sqlite.connection_timeout", 30000);
    }

    // Cache Configuration
    public boolean isCacheEnabled() {
        return config.getBoolean("cache.enabled", true);
    }

    public int getCacheMaxSize() {
        return config.getInt("cache.max_size", 1000);
    }

    public int getCacheExpireAfter() {
        return config.getInt("cache.expire_after", 3600);
    }

    // Messages
    public String getMessagePrefix() {
        return config.getString("messages.prefix", "&8[&bTS&8] &r");
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "Message not found: " + key);
    }
} 
