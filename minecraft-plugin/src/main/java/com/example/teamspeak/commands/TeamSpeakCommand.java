package com.example.teamspeak.commands;

import com.example.teamspeak.TeamSpeakIntegration;
import com.example.teamspeak.teamspeak.TeamSpeakUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamSpeakCommand implements CommandExecutor {
    private final TeamSpeakIntegration plugin;

    public TeamSpeakCommand(TeamSpeakIntegration plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "link":
                if (!player.hasPermission("teamspeak.link")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                handleLink(player);
                break;
            case "unlink":
                if (!player.hasPermission("teamspeak.unlink")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                handleUnlink(player);
                break;
            case "list":
                handleList(player);
                break;
            case "help":
                sendHelp(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /ts help for help.");
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        String prefix = plugin.getConfigManager().getMessagePrefix();
        player.sendMessage(prefix + ChatColor.YELLOW + "TeamSpeak Commands:");
        player.sendMessage(prefix + ChatColor.GRAY + "/ts link " + ChatColor.WHITE + "- Link your Minecraft account with TeamSpeak");
        player.sendMessage(prefix + ChatColor.GRAY + "/ts unlink " + ChatColor.WHITE + "- Unlink your Minecraft account from TeamSpeak");
        player.sendMessage(prefix + ChatColor.GRAY + "/ts list " + ChatColor.WHITE + "- Show online TeamSpeak users");
        player.sendMessage(prefix + ChatColor.GRAY + "/ts help " + ChatColor.WHITE + "- Show this help message");
    }

    private void handleLink(Player player) {
        plugin.getDatabaseManager().isAccountLinked(player.getUniqueId()).thenAccept(isLinked -> {
            if (isLinked) {
                player.sendMessage(plugin.getConfigManager().getMessagePrefix() + 
                    plugin.getConfigManager().getMessage("already_linked"));
                return;
            }

            // Generate a random verification code
            String verificationCode = generateVerificationCode();
            
            // TODO: Implement verification code storage and TeamSpeak message sending
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + 
                ChatColor.YELLOW + "Please connect to TeamSpeak and type the following code in chat: " + 
                ChatColor.GREEN + verificationCode);
        });
    }

    private void handleUnlink(Player player) {
        plugin.getDatabaseManager().unlinkAccount(player.getUniqueId()).thenAccept(success -> {
            if (success) {
                player.sendMessage(plugin.getConfigManager().getMessagePrefix() + 
                    plugin.getConfigManager().getMessage("unlink_success"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessagePrefix() + 
                    plugin.getConfigManager().getMessage("not_linked"));
            }
        });
    }

    private void handleList(Player player) {
        if (!plugin.getTeamSpeakManager().isConnected()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix() + 
                ChatColor.RED + "TeamSpeak server is currently not connected!");
            return;
        }

        player.sendMessage(plugin.getConfigManager().getMessagePrefix() + 
            ChatColor.YELLOW + "Online TeamSpeak Users:");
        
        plugin.getTeamSpeakManager().getUserCache().values().stream()
            .filter(TeamSpeakUser::isOnline)
            .forEach(user -> player.sendMessage(plugin.getConfigManager().getMessagePrefix() + 
                ChatColor.GRAY + "• " + ChatColor.WHITE + user.getUsername()));
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
} 
