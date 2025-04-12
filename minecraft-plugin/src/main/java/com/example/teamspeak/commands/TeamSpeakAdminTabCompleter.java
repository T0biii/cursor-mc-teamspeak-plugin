package com.example.teamspeak.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TeamSpeakAdminTabCompleter implements TabCompleter {
    private static final List<String> COMMANDS = Arrays.asList("reload", "update", "status", "unlink", "list", "verify");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("teamspeak.admin")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("unlink") || args[0].equalsIgnoreCase("verify"))) {
            // Get online player names for unlink and verify commands
            StringUtil.copyPartialMatches(args[1], 
                Bukkit.getOnlinePlayers().stream()
                    .map(player -> player.getName())
                    .collect(Collectors.toList()), 
                completions);
        }

        Collections.sort(completions);
        return completions;
    }
} 
