package com.example.teamspeak.teamspeak;

import com.example.teamspeak.TeamSpeakIntegration;
import com.github.theholydevil.teamspeak.TeamspeakQuery;
import com.github.theholydevil.teamspeak.TeamspeakQueryException;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class TeamSpeakManager {
    private final TeamSpeakIntegration plugin;
    private TeamspeakQuery query;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private BukkitTask updateTask;
    private final Map<String, TeamSpeakUser> userCache = new ConcurrentHashMap<>();

    public TeamSpeakManager(TeamSpeakIntegration plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            query = new TeamspeakQuery(
                plugin.getConfigManager().getTeamSpeakHost(),
                plugin.getConfigManager().getTeamSpeakPort()
            );
            
            query.login(
                plugin.getConfigManager().getTeamSpeakUsername(),
                plugin.getConfigManager().getTeamSpeakPassword()
            );
            
            query.useServer(plugin.getConfigManager().getTeamSpeakVirtualServerId());
            isConnected.set(true);
            plugin.getLogger().info("Successfully connected to TeamSpeak server!");
            
            // Initial user list update
            updateUserList();
        } catch (TeamspeakQueryException e) {
            plugin.getLogger().severe("Failed to connect to TeamSpeak server: " + e.getMessage());
            isConnected.set(false);
        }
    }

    public void disconnect() {
        if (query != null) {
            try {
                query.logout();
            } catch (TeamspeakQueryException e) {
                plugin.getLogger().warning("Error during TeamSpeak logout: " + e.getMessage());
            }
        }
        isConnected.set(false);
    }

    public void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        int interval = plugin.getConfigManager().getTeamSpeakUpdateInterval() * 20; // Convert to ticks
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateUserList, 0L, interval);
    }

    private void updateUserList() {
        if (!isConnected.get()) {
            return;
        }

        try {
            Map<String, Object>[] clients = query.getClients();
            for (Map<String, Object> client : clients) {
                String uid = (String) client.get("client_unique_identifier");
                String username = (String) client.get("client_nickname");
                boolean isOnline = true;
                String[] serverGroups = ((String) client.get("client_servergroups")).split(",");

                // Update cache
                TeamSpeakUser user = new TeamSpeakUser(uid, username, isOnline, serverGroups);
                userCache.put(uid, user);

                // Update database
                plugin.getDatabaseManager().updateTeamSpeakUser(uid, username, isOnline, serverGroups);
            }

            // Mark users not in the list as offline
            userCache.forEach((uid, user) -> {
                boolean found = false;
                for (Map<String, Object> client : clients) {
                    if (uid.equals(client.get("client_unique_identifier"))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    user.setOnline(false);
                    plugin.getDatabaseManager().updateTeamSpeakUser(uid, user.getUsername(), false, user.getServerGroups());
                }
            });

        } catch (TeamspeakQueryException e) {
            plugin.getLogger().severe("Error updating TeamSpeak user list: " + e.getMessage());
            isConnected.set(false);
            // Attempt to reconnect
            Bukkit.getScheduler().runTaskLater(plugin, this::connect, 200L); // 10 seconds delay
        }
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public Map<String, TeamSpeakUser> getUserCache() {
        return userCache;
    }

    public TeamspeakQuery getQuery() {
        return query;
    }
} 
