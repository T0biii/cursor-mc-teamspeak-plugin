package com.example.teamspeak.teamspeak;

import com.example.teamspeak.TeamSpeakIntegration;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Arrays;

public class TeamSpeakManager {
    private final TeamSpeakIntegration plugin;
    private TS3Query query;
    private TS3Api api;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private BukkitTask updateTask;
    private final Map<String, TeamSpeakUser> userCache = new ConcurrentHashMap<>();

    public TeamSpeakManager(TeamSpeakIntegration plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            // Create a new TS3Config
            final TS3Config config = new TS3Config();
            config.setHost(plugin.getConfigManager().getTeamSpeakHost());
            config.setQueryPort(plugin.getConfigManager().getTeamSpeakPort());
            config.setFloodRate(TS3Query.FloodRate.UNLIMITED);

            // Create a new TS3Query
            query = new TS3Query(config);
            query.connect();

            // Get the API
            api = query.getApi();
            
            // Login with server query credentials
            api.login(
                plugin.getConfigManager().getTeamSpeakUsername(),
                plugin.getConfigManager().getTeamSpeakPassword()
            );
            
            // Select the virtual server by port
            api.selectVirtualServerByPort(plugin.getConfigManager().getTeamSpeakVirtualServerPort());
            
            isConnected.set(true);
            plugin.getLogger().info("Successfully connected to TeamSpeak server!");
            
            // Initial user list update
            updateUserList();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to TeamSpeak server: " + e.getMessage());
            isConnected.set(false);
        }
    }

    public void disconnect() {
        if (query != null) {
            query.exit();
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
            // Hole alle Clients auf einmal
            List<Client> clients = api.getClients();
            
            // Hole alle Servergruppen auf einmal
            List<ServerGroup> allServerGroups = api.getServerGroups();
            
            // Erstelle eine Map für schnellen Zugriff auf Servergruppen nach Client-ID
            Map<Integer, List<ServerGroup>> clientGroupsMap = new HashMap<>();
            
            // Hole die Servergruppen für alle Clients auf einmal
            for (Client client : clients) {
                int clientId = client.getId();
                int[] clientGroupIds = client.getServerGroups();
                List<ServerGroup> clientGroups = allServerGroups.stream()
                    .filter(group -> Arrays.stream(clientGroupIds).anyMatch(id -> id == group.getId()))
                    .collect(Collectors.toList());
                clientGroupsMap.put(clientId, clientGroups);
            }
            
            // Verarbeite die Clients
            for (Client client : clients) {
                String uid = client.getUniqueIdentifier();
                String username = client.getNickname();
                boolean isOnline = true;
                
                // Hole die Servergruppen aus der Map
                List<ServerGroup> serverGroups = clientGroupsMap.get(client.getId());
                String[] groupIds = serverGroups.stream()
                    .map(group -> String.valueOf(group.getId()))
                    .toArray(String[]::new);

                // Update cache
                TeamSpeakUser user = new TeamSpeakUser(uid, username, isOnline, groupIds);
                userCache.put(uid, user);

                // Update database
                plugin.getDatabaseManager().updateTeamSpeakUser(uid, username, isOnline, groupIds);
            }

            // Mark users not in the list as offline
            userCache.forEach((uid, user) -> {
                boolean found = clients.stream()
                    .anyMatch(client -> uid.equals(client.getUniqueIdentifier()));
                if (!found) {
                    user.setOnline(false);
                    plugin.getDatabaseManager().updateTeamSpeakUser(uid, user.getUsername(), false, user.getServerGroups());
                }
            });

        } catch (Exception e) {
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

    public TS3Api getApi() {
        return api;
    }

    public void addPlayerToGroup(String uid, int groupId) {
        if (!isConnected.get()) {
            return;
        }

        try {
            Client client = api.getClients().stream()
                .filter(c -> c.getUniqueIdentifier().equals(uid))
                .findFirst()
                .orElse(null);
                
            if (client != null) {
                api.addClientToServerGroup(groupId, client.getDatabaseId());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error adding player to TeamSpeak group: " + e.getMessage());
        }
    }
} 
