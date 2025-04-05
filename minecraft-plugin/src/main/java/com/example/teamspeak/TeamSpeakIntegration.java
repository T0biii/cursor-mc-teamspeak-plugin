package com.example.teamspeak;

import com.example.teamspeak.config.ConfigManager;
import com.example.teamspeak.database.DatabaseManager;
import com.example.teamspeak.teamspeak.TeamSpeakManager;
import com.example.teamspeak.commands.TeamSpeakCommand;
import com.example.teamspeak.commands.TeamSpeakAdminCommand;
import com.example.teamspeak.commands.TeamSpeakAdminTabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public class TeamSpeakIntegration extends JavaPlugin {
    private static TeamSpeakIntegration instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private TeamSpeakManager teamSpeakManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.teamSpeakManager = new TeamSpeakManager(this);
        
        // Register commands
        getCommand("ts").setExecutor(new TeamSpeakCommand(this));
        getCommand("tsadmin").setExecutor(new TeamSpeakAdminCommand(this));
        getCommand("tsadmin").setTabCompleter(new TeamSpeakAdminTabCompleter());
        
        // Start TeamSpeak connection and user list updates
        teamSpeakManager.connect();
        teamSpeakManager.startUpdateTask();
        
        getLogger().info("TeamSpeak Integration has been enabled!");
    }

    @Override
    public void onDisable() {
        if (teamSpeakManager != null) {
            teamSpeakManager.disconnect();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        getLogger().info("TeamSpeak Integration has been disabled!");
    }

    public static TeamSpeakIntegration getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TeamSpeakManager getTeamSpeakManager() {
        return teamSpeakManager;
    }
} 
