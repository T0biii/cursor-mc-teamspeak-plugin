package com.example.teamspeak.commands;

import com.example.teamspeak.TeamSpeakIntegration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TeamSpeakAdminCommand implements CommandExecutor {
    private final TeamSpeakIntegration plugin;

    public TeamSpeakAdminCommand(TeamSpeakIntegration plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("teamspeak.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
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
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /tsadmin help for help.");
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        String prefix = plugin.getConfigManager().getMessagePrefix();
        sender.sendMessage(prefix + ChatColor.YELLOW + "TeamSpeak Admin Commands:");
        sender.sendMessage(prefix + ChatColor.GRAY + "/tsadmin reload " + ChatColor.WHITE + "- Reload the plugin configuration");
        sender.sendMessage(prefix + ChatColor.GRAY + "/tsadmin update " + ChatColor.WHITE + "- Force update TeamSpeak user list");
        sender.sendMessage(prefix + ChatColor.GRAY + "/tsadmin status " + ChatColor.WHITE + "- Show plugin status");
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadConfig();
        sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + 
            ChatColor.GREEN + "Configuration reloaded!");
    }

    private void handleUpdate(CommandSender sender) {
        if (!plugin.getTeamSpeakManager().isConnected()) {
            sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + 
                ChatColor.RED + "TeamSpeak server is not connected!");
            return;
        }

        plugin.getTeamSpeakManager().startUpdateTask();
        sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + 
            ChatColor.GREEN + "TeamSpeak user list update triggered!");
    }

    private void handleStatus(CommandSender sender) {
        String prefix = plugin.getConfigManager().getMessagePrefix();
        sender.sendMessage(prefix + ChatColor.YELLOW + "TeamSpeak Integration Status:");
        
        // Connection status
        boolean isConnected = plugin.getTeamSpeakManager().isConnected();
        sender.sendMessage(prefix + ChatColor.GRAY + "TeamSpeak Connection: " + 
            (isConnected ? ChatColor.GREEN + "Connected" : ChatColor.RED + "Disconnected"));
        
        // User count
        int onlineUsers = (int) plugin.getTeamSpeakManager().getUserCache().values().stream()
            .filter(user -> user.isOnline())
            .count();
        sender.sendMessage(prefix + ChatColor.GRAY + "Online Users: " + 
            ChatColor.WHITE + onlineUsers);
        
        // Database status
        try {
            plugin.getDatabaseManager().isAccountLinked(java.util.UUID.randomUUID())
                .thenAccept(result -> sender.sendMessage(prefix + ChatColor.GRAY + "Database Connection: " + 
                    ChatColor.GREEN + "Connected"));
        } catch (Exception e) {
            sender.sendMessage(prefix + ChatColor.GRAY + "Database Connection: " + 
                ChatColor.RED + "Error");
        }
    }
} 
