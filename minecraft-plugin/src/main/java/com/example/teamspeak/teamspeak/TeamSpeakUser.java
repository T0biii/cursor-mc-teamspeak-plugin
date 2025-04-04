package com.example.teamspeak.teamspeak;

public class TeamSpeakUser {
    private final String uid;
    private final String username;
    private boolean online;
    private final String[] serverGroups;

    public TeamSpeakUser(String uid, String username, boolean online, String[] serverGroups) {
        this.uid = uid;
        this.username = username;
        this.online = online;
        this.serverGroups = serverGroups;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String[] getServerGroups() {
        return serverGroups;
    }

    public boolean hasServerGroup(String groupId) {
        for (String group : serverGroups) {
            if (group.equals(groupId)) {
                return true;
            }
        }
        return false;
    }
} 
