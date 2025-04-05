package com.example.teamspeak.commands;

import com.example.teamspeak.TeamSpeakIntegration;
import com.example.teamspeak.teamspeak.TeamSpeakUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
} 
