package com.example.teamspeak.config;

import com.example.teamspeak.TeamSpeakIntegration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final TeamSpeakIntegration plugin;
    private FileConfiguration config;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;

    public ConfigManager(TeamSpeakIntegration plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();
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

    public int getTeamSpeakVirtualServerPort() {
        return config.getInt("teamspeak.virtual_server_port", 9987);
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
    public Component getMessagePrefix() {
        String rawPrefix = config.getString("messages.prefix", "<dark_gray>[<aqua>TS</aqua>]</dark_gray> ");
        return miniMessage.deserialize(rawPrefix);
    }

    /**
     * Konvertiert Legacy-Farbcodes in Adventure Components
     * @param text Der Text mit Legacy-Farbcodes
     * @return Der Text als Adventure Component
     */
    public Component translateColors(String text) {
        if (text == null) return Component.empty();
        // Erst Legacy-Farbcodes übersetzen
        String legacyText = legacySerializer.serialize(legacySerializer.deserialize(text));
        // Dann in MiniMessage Format konvertieren
        return miniMessage.deserialize(legacyText);
    }

    public Component getMessage(String key) {
        String rawMessage = config.getString("messages." + key, "Message not found: " + key);
        return translateColors(rawMessage);
    }
} 
