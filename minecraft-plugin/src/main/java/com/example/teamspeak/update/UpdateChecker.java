package com.example.teamspeak.update;

import com.example.teamspeak.TeamSpeakIntegration;
import org.bukkit.Bukkit;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class UpdateChecker {
    private final TeamSpeakIntegration plugin;
    private final String currentVersion;
    private final String githubRepo;

    public UpdateChecker(TeamSpeakIntegration plugin, String githubRepo) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.githubRepo = githubRepo;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + githubRepo + "/releases/latest");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String latestVersion = jsonResponse.getString("tag_name").replace("v", "");
                    
                    if (!currentVersion.equals(latestVersion)) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.getLogger().log(Level.INFO, "§a[TeamSpeak Integration] Eine neue Version ist verfügbar!");
                            plugin.getLogger().log(Level.INFO, "§aAktuelle Version: " + currentVersion);
                            plugin.getLogger().log(Level.INFO, "§aNeueste Version: " + latestVersion);
                            plugin.getLogger().log(Level.INFO, "§aDownload: https://github.com/" + githubRepo + "/releases/latest");
                        });
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Fehler beim Prüfen auf Updates: " + e.getMessage());
            }
        });
    }
} 
