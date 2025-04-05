package com.example.teamspeak.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TeamSpeakAdminTabCompleter implements TabCompleter {
    private static final List<String> COMMANDS = Arrays.asList("reload", "update", "status");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("teamspeak.admin")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        }

        Collections.sort(completions);
        return completions;
    }
} 
