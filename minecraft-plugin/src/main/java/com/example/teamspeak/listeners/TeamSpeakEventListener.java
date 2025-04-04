package com.example.teamspeak.listeners;

import com.example.teamspeak.TeamSpeakIntegration;
import com.example.teamspeak.teamspeak.TeamSpeakUser;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import java.util.concurrent.CompletableFuture;

public class TeamSpeakEventListener {
    private final TeamSpeakIntegration plugin;
    private final TS3Api api;

    public TeamSpeakEventListener(TeamSpeakIntegration plugin, TS3Api api) {
        this.plugin = plugin;
        this.api = api;
    }

    /**
     * Handles a TeamSpeak user joining the server
     * @param client The client information from TeamSpeak
     */
    public void handleUserJoin(Client client) {
        String uid = client.getUniqueIdentifier();
        String username = client.getNickname();
        String[] serverGroups = api.getServerGroupsByClientId(client.getId()).stream()
            .map(group -> String.valueOf(group.getId()))
            .toArray(String[]::new);

        // Update the user in the cache
        TeamSpeakUser user = new TeamSpeakUser(uid, username, true, serverGroups);
        plugin.getTeamSpeakManager().getUserCache().put(uid, user);

        // Update the database
        plugin.getDatabaseManager().updateTeamSpeakUser(uid, username, true, serverGroups);

        // Check if this user has a linked Minecraft account and update the username
        updateLinkedMinecraftAccount(uid, username);
    }

    /**
     * Handles a TeamSpeak user leaving the server
     * @param uid The user's unique identifier
     */
    public void handleUserLeave(String uid) {
        // Mark the user as offline in the cache
        TeamSpeakUser user = plugin.getTeamSpeakManager().getUserCache().get(uid);
        if (user != null) {
            user.setOnline(false);
            plugin.getDatabaseManager().updateTeamSpeakUser(uid, user.getUsername(), false, user.getServerGroups());
        }
    }

    /**
     * Updates the TeamSpeak username for a linked Minecraft account
     * @param teamspeakUid The TeamSpeak user's unique identifier
     * @param teamspeakUsername The TeamSpeak user's current username
     */
    private void updateLinkedMinecraftAccount(String teamspeakUid, String teamspeakUsername) {
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE linked_accounts SET teamspeak_username = ? WHERE teamspeak_uid = ?";
            
            try (java.sql.Connection conn = plugin.getDatabaseManager().getDataSource().getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, teamspeakUsername);
                stmt.setString(2, teamspeakUid);
                stmt.executeUpdate();
                
                plugin.getLogger().info("Updated TeamSpeak username for " + teamspeakUsername + " in the database");
            } catch (java.sql.SQLException e) {
                plugin.getLogger().severe("Error updating TeamSpeak username: " + e.getMessage());
            }
        });
    }

    /**
     * Handles a TeamSpeak user changing their nickname
     * @param uid The user's unique identifier
     * @param newNickname The user's new nickname
     */
    public void handleNicknameChange(String uid, String newNickname) {
        // Update the user in the cache
        TeamSpeakUser user = plugin.getTeamSpeakManager().getUserCache().get(uid);
        if (user != null) {
            user = new TeamSpeakUser(uid, newNickname, user.isOnline(), user.getServerGroups());
            plugin.getTeamSpeakManager().getUserCache().put(uid, user);
            
            // Update the database
            plugin.getDatabaseManager().updateTeamSpeakUser(uid, newNickname, user.isOnline(), user.getServerGroups());
            
            // Update the linked Minecraft account
            updateLinkedMinecraftAccount(uid, newNickname);
        }
    }

    /**
     * Handles a TeamSpeak user's server groups changing
     * @param uid The user's unique identifier
     * @param serverGroups The user's new server groups
     */
    public void handleServerGroupsChange(String uid, String[] serverGroups) {
        // Update the user in the cache
        TeamSpeakUser user = plugin.getTeamSpeakManager().getUserCache().get(uid);
        if (user != null) {
            user = new TeamSpeakUser(uid, user.getUsername(), user.isOnline(), serverGroups);
            plugin.getTeamSpeakManager().getUserCache().put(uid, user);
            
            // Update the database
            plugin.getDatabaseManager().updateTeamSpeakUser(uid, user.getUsername(), user.isOnline(), serverGroups);
        }
    }
} 
