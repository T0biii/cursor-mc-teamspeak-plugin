package de.t0biii.teamspeak;

import de.t0biii.teamspeak.config.ConfigManager;
import de.t0biii.teamspeak.database.DatabaseManager;
import de.t0biii.teamspeak.teamspeak.TeamSpeakManager;
import de.t0biii.teamspeak.commands.TeamSpeakCommand;
import de.t0biii.teamspeak.commands.TeamSpeakAdminCommand;
import de.t0biii.teamspeak.commands.TeamSpeakAdminTabCompleter;
import de.t0biii.teamspeak.update.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

public class TeamSpeakIntegration extends JavaPlugin {
    private static TeamSpeakIntegration instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private TeamSpeakManager teamSpeakManager;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.teamSpeakManager = new TeamSpeakManager(this);
        
        // Initialize update checker
        this.updateChecker = new UpdateChecker(this, "T0biii/cursor-mc-teamspeak-plugin");
        this.updateChecker.checkForUpdates();
        
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
