package com.example.teamspeak.commands;

import com.example.teamspeak.TeamSpeakIntegration;
import com.example.teamspeak.teamspeak.TeamSpeakUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.util.StringUtil;

import java.util.*;

public class TeamSpeakCommand implements CommandExecutor, TabCompleter {
    private final TeamSpeakIntegration plugin;
    private static final List<String> COMMANDS = Arrays.asList("link", "unlink", "list", "help");

    public TeamSpeakCommand(TeamSpeakIntegration plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // Nur Vorschläge anzeigen, wenn der Sender die Berechtigung hat
        if (!(sender instanceof Player)) {
            return completions;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            // Zeige nur die Befehle an, für die der Spieler eine Berechtigung hat
            List<String> availableCommands = new ArrayList<>();
            if (player.hasPermission("teamspeak.link")) availableCommands.add("link");
            if (player.hasPermission("teamspeak.unlink")) availableCommands.add("unlink");
            availableCommands.add("list");
            availableCommands.add("help");

            StringUtil.copyPartialMatches(args[0], availableCommands, completions);
        }

        Collections.sort(completions); // Alphabetisch sortieren
        return completions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
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
                    player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                    return true;
                }
                handleLink(player);
                break;
            case "unlink":
                if (!player.hasPermission("teamspeak.unlink")) {
                    player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
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
                player.sendMessage(Component.text("Unknown subcommand. Use /ts help for help.", NamedTextColor.RED));
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        Component prefix = plugin.getConfigManager().getMessagePrefix();
        player.sendMessage(prefix.append(Component.text("TeamSpeak Commands:", NamedTextColor.YELLOW)));
        
        // Link Command
        player.sendMessage(prefix.append(
            Component.text("/ts link", NamedTextColor.GRAY)
                .hoverEvent(Component.text("Click to link your Minecraft account with TeamSpeak", NamedTextColor.WHITE))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/ts link"))
            ).append(Component.text(" - Link your Minecraft account with TeamSpeak", NamedTextColor.WHITE)));

        // Unlink Command
        player.sendMessage(prefix.append(
            Component.text("/ts unlink", NamedTextColor.GRAY)
                .hoverEvent(Component.text("Click to unlink your Minecraft account from TeamSpeak", NamedTextColor.WHITE))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/ts unlink"))
            ).append(Component.text(" - Unlink your Minecraft account from TeamSpeak", NamedTextColor.WHITE)));

        // List Command
        player.sendMessage(prefix.append(
            Component.text("/ts list", NamedTextColor.GRAY)
                .hoverEvent(Component.text("Click to show online TeamSpeak users", NamedTextColor.WHITE))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/ts list"))
            ).append(Component.text(" - Show online TeamSpeak users", NamedTextColor.WHITE)));

        // Help Command
        player.sendMessage(prefix.append(
            Component.text("/ts help", NamedTextColor.GRAY)
                .hoverEvent(Component.text("Click to show this help message", NamedTextColor.WHITE))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/ts help"))
            ).append(Component.text(" - Show this help message", NamedTextColor.WHITE)));
    }

    private void handleLink(Player player) {
        plugin.getDatabaseManager().isAccountLinked(player.getUniqueId()).thenAccept(isLinked -> {
            if (isLinked) {
                player.sendMessage(plugin.getConfigManager().getMessagePrefix()
                    .append(plugin.getConfigManager().getMessage("already_linked")));
                return;
            }

            String verificationCode = generateVerificationCode();
            
            player.sendMessage(plugin.getConfigManager().getMessagePrefix()
                .append(Component.text("Please connect to TeamSpeak and type the following code in chat: ", NamedTextColor.YELLOW))
                .append(Component.text(verificationCode, NamedTextColor.GREEN)));
        });
    }

    private void handleUnlink(Player player) {
        plugin.getDatabaseManager().unlinkAccount(player.getUniqueId()).thenAccept(success -> {
            if (success) {
                player.sendMessage(plugin.getConfigManager().getMessagePrefix()
                    .append(plugin.getConfigManager().getMessage("unlink_success")));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessagePrefix()
                    .append(plugin.getConfigManager().getMessage("not_linked")));
            }
        });
    }

    private void handleList(Player player) {
        if (!plugin.getTeamSpeakManager().isConnected()) {
            player.sendMessage(plugin.getConfigManager().getMessagePrefix()
                .append(Component.text("TeamSpeak server is currently not connected!", NamedTextColor.RED)));
            return;
        }

        player.sendMessage(plugin.getConfigManager().getMessagePrefix()
            .append(Component.text("Online TeamSpeak Users:", NamedTextColor.YELLOW)));
        
        plugin.getTeamSpeakManager().getUserCache().values().stream()
            .filter(TeamSpeakUser::isOnline)
            .forEach(user -> player.sendMessage(plugin.getConfigManager().getMessagePrefix()
                .append(Component.text("• ", NamedTextColor.GRAY))
                .append(Component.text(user.getUsername(), NamedTextColor.WHITE))));
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
} 
