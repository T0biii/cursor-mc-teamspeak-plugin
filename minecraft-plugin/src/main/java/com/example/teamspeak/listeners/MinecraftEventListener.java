package com.example.teamspeak.listeners;

import com.example.teamspeak.TeamSpeakIntegration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MinecraftEventListener implements Listener {
    private final TeamSpeakIntegration plugin;

    public MinecraftEventListener(TeamSpeakIntegration plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        String playerName = player.getName();

        // Check if the player has a linked TeamSpeak account
        plugin.getDatabaseManager().isAccountLinked(playerUuid).thenAccept(isLinked -> {
            if (isLinked) {
                // Update the Minecraft username in the database
                updateMinecraftUsername(playerUuid, playerName);
                
                // Check if the player has a rank that should be synced with TeamSpeak
                syncPlayerRank(player);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // We don't need to do anything special on quit
        // The TeamSpeak manager will handle marking users as offline
    }

    /**
     * Updates the Minecraft username in the database
     * @param playerUuid The player's UUID
     * @param playerName The player's current username
     */
    private void updateMinecraftUsername(UUID playerUuid, String playerName) {
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE linked_accounts SET minecraft_username = ? WHERE minecraft_uuid = ?";
            
            try (java.sql.Connection conn = plugin.getDatabaseManager().getDataSource().getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                stmt.setString(2, playerUuid.toString());
                stmt.executeUpdate();
                
                plugin.getLogger().info("Updated Minecraft username for " + playerName + " in the database");
            } catch (java.sql.SQLException e) {
                plugin.getLogger().severe("Error updating Minecraft username: " + e.getMessage());
            }
        });
    }

    /**
     * Syncs the player's Minecraft rank with TeamSpeak
     * @param player The player to sync
     */
    private void syncPlayerRank(Player player) {
        // Get the player's primary group/rank
        String rank = getPlayerRank(player);
        
        // Get the TeamSpeak UID for this player
        CompletableFuture.runAsync(() -> {
            String sql = "SELECT teamspeak_uid FROM linked_accounts WHERE minecraft_uuid = ?";
            
            try (java.sql.Connection conn = plugin.getDatabaseManager().getDataSource().getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.getUniqueId().toString());
                java.sql.ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String teamspeakUid = rs.getString("teamspeak_uid");
                    
                    // Get the TeamSpeak group ID for this rank
                    sql = "SELECT teamspeak_group_id FROM role_mappings WHERE minecraft_rank = ?";
                    try (java.sql.PreparedStatement rankStmt = conn.prepareStatement(sql)) {
                        rankStmt.setString(1, rank);
                        java.sql.ResultSet rankRs = rankStmt.executeQuery();
                        
                        if (rankRs.next()) {
                            int teamspeakGroupId = rankRs.getInt("teamspeak_group_id");
                            
                            // Add the player to the TeamSpeak group
                            plugin.getTeamSpeakManager().addPlayerToGroup(teamspeakUid, teamspeakGroupId);
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                plugin.getLogger().severe("Error syncing player rank: " + e.getMessage());
            }
        });
    }

    /**
     * Gets the player's primary rank/group
     * @param player The player to get the rank for
     * @return The player's primary rank
     */
    private String getPlayerRank(Player player) {
        // This is a placeholder - you would need to implement this based on your permission plugin
        // For example, with LuckPerms:
        // return LuckPermsProvider.get().getUserManager().loadUser(player.getUniqueId()).join().getPrimaryGroup();
        
        // For now, we'll just return "default"
        return "default";
    }
} 
