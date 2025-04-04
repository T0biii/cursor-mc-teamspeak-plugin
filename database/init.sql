-- Create tables for Minecraft-TeamSpeak integration

-- Table for storing linked accounts
CREATE TABLE linked_accounts (
    id SERIAL PRIMARY KEY,
    minecraft_uuid VARCHAR(36) NOT NULL UNIQUE,
    minecraft_username VARCHAR(16) NOT NULL,
    teamspeak_uid VARCHAR(28) NOT NULL UNIQUE,
    teamspeak_username VARCHAR(64) NOT NULL,
    linked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_verified TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table for caching TeamSpeak users
CREATE TABLE teamspeak_users_cache (
    id SERIAL PRIMARY KEY,
    teamspeak_uid VARCHAR(28) NOT NULL UNIQUE,
    username VARCHAR(64) NOT NULL,
    last_seen TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_online BOOLEAN DEFAULT false,
    server_groups TEXT[] DEFAULT '{}'
);

-- Table for storing role mappings
CREATE TABLE role_mappings (
    id SERIAL PRIMARY KEY,
    minecraft_rank VARCHAR(32) NOT NULL,
    teamspeak_group_id INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_linked_accounts_minecraft_uuid ON linked_accounts(minecraft_uuid);
CREATE INDEX idx_linked_accounts_teamspeak_uid ON linked_accounts(teamspeak_uid);
CREATE INDEX idx_teamspeak_users_cache_uid ON teamspeak_users_cache(teamspeak_uid);
CREATE INDEX idx_role_mappings_minecraft_rank ON role_mappings(minecraft_rank); 
