package com.example.teamspeak.commands;

import com.example.teamspeak.TeamSpeakIntegration;
import com.example.teamspeak.teamspeak.TeamSpeakUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamSpeakAdminCommand implements CommandExecutor {
    private final TeamSpeakIntegration plugin;

    public TeamSpeakAdminCommand(TeamSpeakIntegration plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("teamspeak.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "update":
                handleUpdate(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "unlink":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /tsadmin unlink <player>", NamedTextColor.RED));
                    return true;
                }
                handleUnlink(sender, args[1]);
                break;
            case "list":
                handleList(sender);
                break;
            case "verify":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /tsadmin verify <player>", NamedTextColor.RED));
                    return true;
                }
                handleVerify(sender, args[1]);
                break;
            default:
                sender.sendMessage(Component.text("Unknown subcommand. Use /tsadmin help for help.", NamedTextColor.RED));
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        Component prefix = plugin.getConfigManager().getMessagePrefix();
        sender.sendMessage(prefix.append(Component.text("TeamSpeak Admin Commands:", NamedTextColor.YELLOW)));
        
        // Reload command
        sender.sendMessage(prefix.append(
            Component.text("/tsadmin reload", NamedTextColor.GRAY)
                .hoverEvent(Component.text("Click to reload the plugin configuration"))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tsadmin reload"))
            ).append(Component.text(" - Reload the plugin configuration", NamedTextColor.WHITE)));
        
        // Update command
        sender.sendMessage(prefix.append(
            Component.text("/tsadmin update", NamedTextColor.GRAY)
                .hoverEvent(Component.text("Click to force update TeamSpeak user list"))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tsadmin update"))
            ).append(Component.text(" - Force update TeamSpeak user list", NamedTextColor.WHITE)));
        
        // Status command
        sender.sendMessage(prefix.append(
            Component.text("/tsadmin status", NamedTextColor.GRAY)
                .hoverEvent(Component.text("Click to show plugin status"))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tsadmin status"))
            ).append(Component.text(" - Show plugin status", NamedTextColor.WHITE)));

        // Unlink command
        sender.sendMessage(prefix.append(
            Component.text("/tsadmin unlink <player>", NamedTextColor.GRAY)
                .hoverEvent(Component.text("Click to unlink a player's TeamSpeak account"))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tsadmin unlink"))
            ).append(Component.text(" - Unlink a player's TeamSpeak account", NamedTextColor.WHITE)));

        // List command
        sender.sendMessage(prefix.append(
            Component.text("/tsadmin list", NamedTextColor.GRAY)
                .hoverEvent(Component.text("Click to show all linked accounts"))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tsadmin list"))
            ).append(Component.text(" - Show all linked accounts", NamedTextColor.WHITE)));

        // Verify command
        sender.sendMessage(prefix.append(
            Component.text("/tsadmin verify <player>", NamedTextColor.GRAY)
                .hoverEvent(Component.text("Click to manually verify a player's account"))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tsadmin verify"))
            ).append(Component.text(" - Manually verify a player's account", NamedTextColor.WHITE)));
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadConfig();
        sender.sendMessage(plugin.getConfigManager().getMessagePrefix()
            .append(Component.text("Configuration reloaded!", NamedTextColor.GREEN)));
    }

    private void handleUpdate(CommandSender sender) {
        if (!plugin.getTeamSpeakManager().isConnected()) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix()
                .append(Component.text("TeamSpeak server is not connected!", NamedTextColor.RED)));
            return;
        }

        plugin.getTeamSpeakManager().startUpdateTask();
        sender.sendMessage(plugin.getConfigManager().getMessagePrefix()
            .append(Component.text("TeamSpeak user list update triggered!", NamedTextColor.GREEN)));
    }

    private void handleStatus(CommandSender sender) {
        Component prefix = plugin.getConfigManager().getMessagePrefix();
        sender.sendMessage(prefix.append(Component.text("TeamSpeak Integration Status:", NamedTextColor.YELLOW)));
        
        // Connection status
        boolean isConnected = plugin.getTeamSpeakManager().isConnected();
        sender.sendMessage(prefix.append(Component.text("TeamSpeak Connection: ", NamedTextColor.GRAY))
            .append(Component.text(isConnected ? "Connected" : "Disconnected", 
                isConnected ? NamedTextColor.GREEN : NamedTextColor.RED)));

        // TeamSpeak Server Info
        String tsHost = plugin.getConfigManager().getTeamSpeakHost();
        int tsPort = plugin.getConfigManager().getTeamSpeakVirtualServerPort();
        sender.sendMessage(prefix.append(Component.text("TeamSpeak Server: ", NamedTextColor.GRAY))
            .append(Component.text(tsHost + ":" + tsPort, NamedTextColor.YELLOW)));
        
        // Database Type
        String dbType = plugin.getConfigManager().getDatabaseType();
        sender.sendMessage(prefix.append(Component.text("Database Type: ", NamedTextColor.GRAY))
            .append(Component.text(dbType.toUpperCase(), NamedTextColor.YELLOW)));

        // Database Connection Status
        try {
            plugin.getDatabaseManager().isAccountLinked(java.util.UUID.randomUUID())
                .thenAccept(result -> sender.sendMessage(prefix
                    .append(Component.text("Database Connection: ", NamedTextColor.GRAY))
                    .append(Component.text("Connected", NamedTextColor.GREEN))));
        } catch (Exception e) {
            sender.sendMessage(prefix.append(Component.text("Database Connection: ", NamedTextColor.GRAY))
                .append(Component.text("Error", NamedTextColor.RED)));
        }
        
        // User count
        int onlineUsers = (int) plugin.getTeamSpeakManager().getUserCache().values().stream()
            .filter(TeamSpeakUser::isOnline)
            .count();
        sender.sendMessage(prefix.append(Component.text("Online Users: ", NamedTextColor.GRAY))
            .append(Component.text(String.valueOf(onlineUsers), NamedTextColor.YELLOW)));

        // Show online users
        if (isConnected && onlineUsers > 0) {
            sender.sendMessage(prefix.append(Component.text("Online TeamSpeak Users:", NamedTextColor.YELLOW)));
            plugin.getTeamSpeakManager().getUserCache().values().stream()
                .filter(TeamSpeakUser::isOnline)
                .forEach(user -> sender.sendMessage(prefix
                    .append(Component.text("• ", NamedTextColor.GRAY))
                    .append(Component.text(user.getUsername(), NamedTextColor.WHITE))));
        }
    }

    private void handleUnlink(CommandSender sender, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix()
                .append(Component.text("Player not found: " + playerName, NamedTextColor.RED)));
            return;
        }

        plugin.getDatabaseManager().unlinkAccount(targetPlayer.getUniqueId()).thenAccept(success -> {
            if (success) {
                sender.sendMessage(plugin.getConfigManager().getMessagePrefix()
                    .append(Component.text("Successfully unlinked " + playerName + "'s TeamSpeak account!", NamedTextColor.GREEN)));
                targetPlayer.sendMessage(plugin.getConfigManager().getMessagePrefix()
                    .append(Component.text("Your TeamSpeak account has been unlinked by an administrator.", NamedTextColor.YELLOW)));
            } else {
                sender.sendMessage(plugin.getConfigManager().getMessagePrefix()
                    .append(Component.text("Failed to unlink " + playerName + "'s TeamSpeak account. Account might not be linked.", NamedTextColor.RED)));
            }
        });
    }

    private void handleList(CommandSender sender) {
        Component prefix = plugin.getConfigManager().getMessagePrefix();
        sender.sendMessage(prefix.append(Component.text("Linked Accounts:", NamedTextColor.YELLOW)));

        plugin.getDatabaseManager().getAllLinkedAccounts().thenAccept(accounts -> {
            if (accounts.isEmpty()) {
                sender.sendMessage(prefix.append(Component.text("No accounts are currently linked.", NamedTextColor.GRAY)));
                return;
            }

            accounts.forEach(account -> {
                String playerName = Bukkit.getOfflinePlayer(UUID.fromString(account.getMinecraftUuid())).getName();
                if (playerName != null) {
                    sender.sendMessage(prefix
                        .append(Component.text("• ", NamedTextColor.GRAY))
                        .append(Component.text(playerName, NamedTextColor.WHITE))
                        .append(Component.text(" → ", NamedTextColor.GRAY))
                        .append(Component.text(account.getTeamSpeakUsername(), NamedTextColor.AQUA)));
                }
            });
        });
    }

    private void handleVerify(CommandSender sender, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix()
                .append(Component.text("Player not found: " + playerName, NamedTextColor.RED)));
            return;
        }

        plugin.getDatabaseManager().isAccountLinked(targetPlayer.getUniqueId()).thenAccept(isLinked -> {
            if (!isLinked) {
                sender.sendMessage(plugin.getConfigManager().getMessagePrefix()
                    .append(Component.text("Player " + playerName + " does not have a linked TeamSpeak account.", NamedTextColor.RED)));
                return;
            }

            // Generate a new verification code
            String verificationCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Send the verification code to both the admin and the player
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix()
                .append(Component.text("Verification code for " + playerName + ": ", NamedTextColor.YELLOW))
                .append(Component.text(verificationCode, NamedTextColor.GREEN)));
            
            targetPlayer.sendMessage(plugin.getConfigManager().getMessagePrefix()
                .append(Component.text("An administrator has initiated account verification. ", NamedTextColor.YELLOW))
                .append(Component.text("Please enter this code in TeamSpeak: ", NamedTextColor.YELLOW))
                .append(Component.text(verificationCode, NamedTextColor.GREEN)));
        });
    }
} 
