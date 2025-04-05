# Minecraft-TeamSpeak Integration Plugin - Admin Guide

## Introduction
This guide provides detailed information for server administrators on how to set up, configure, and maintain the Minecraft-TeamSpeak Integration Plugin.

## Installation

### Prerequisites
- Java 17 or higher
- Minecraft server (Paper/Spigot 1.20.x or higher)
- TeamSpeak 3 Server
- PostgreSQL (optional) or SQLite

### Installation Steps
1. Download the latest release from GitHub
2. Place the JAR file in your server's `plugins` directory
3. Start/restart your Minecraft server
4. Configure the plugin using the generated `config.yml`

## Configuration

### Basic Configuration
```yaml
# config.yml
database:
  type: "sqlite"  # or "postgres"
  postgres:
    host: "localhost"
    port: 5432
    database: "minecraft_teamspeak"
    username: "minecraft"
    password: "your_password"
  sqlite:
    file: "plugins/TeamSpeakIntegration/database.db"

teamspeak:
  host: "your-teamspeak-server"
  port: 10011
  username: "serverquery"
  password: "your-query-password"
  virtual_server_id: 1
```

### Advanced Settings
```yaml
# Additional settings in config.yml
update_interval: 60  # Seconds between TeamSpeak updates
cache_timeout: 300   # Seconds before cache invalidation
debug_mode: false    # Enable detailed logging
```

## Admin Commands

### Plugin Management
```
/tsadmin reload    - Reload the plugin configuration
/tsadmin update    - Force update TeamSpeak user list
/tsadmin status    - Show plugin status and statistics
```

### User Management
```
/tsadmin unlink <player>  - Unlink a player's TeamSpeak account
/tsadmin list            - Show all linked accounts
/tsadmin verify <player> - Manually verify a player's account
```

## Database Management

### PostgreSQL Setup
1. Create a new database
2. Run the initialization script from `database/init.sql`
3. Update the configuration with your database credentials

### SQLite Maintenance
- The database file is automatically created
- Regular backups are recommended
- Location: `plugins/TeamSpeakIntegration/database.db`

## Role Management

### Default Role Configuration
```yaml
roles:
  default: 1        # Default TeamSpeak role ID
  admin: 2          # Admin role ID
  moderator: 3      # Moderator role ID
  vip: 4            # VIP role ID
```

### Role Synchronization
- Roles are automatically synchronized based on Minecraft permissions
- Updates occur when:
  - Player joins the server
  - Player's rank changes
  - TeamSpeak roles are modified

## Troubleshooting

### Common Issues
1. **TeamSpeak Connection Failed**
   - Verify TeamSpeak server is running
   - Check query credentials
   - Ensure correct port is open

2. **Database Connection Issues**
   - Verify database credentials
   - Check database server status
   - Ensure proper permissions

3. **Role Synchronization Problems**
   - Check role IDs in configuration
   - Verify TeamSpeak permissions
   - Check Minecraft permission nodes

### Logging
- Logs are stored in `plugins/TeamSpeakIntegration/logs/`
- Enable debug mode for detailed logging
- Regular log rotation is implemented

## Security Considerations

### Best Practices
1. Use strong passwords for all services
2. Regularly update the plugin
3. Monitor logs for suspicious activity
4. Implement rate limiting
5. Regular backup of database

### Permission Nodes
```
teamspeak.admin     - Access to admin commands
teamspeak.reload    - Permission to reload plugin
teamspeak.update    - Permission to force updates
teamspeak.verify    - Permission to verify accounts
```

## Updates and Maintenance

### Update Process
1. Backup your configuration and database
2. Download the new release
3. Replace the old JAR file
4. Restart the server
5. Check logs for any issues

### Backup Strategy
1. Regular database backups
2. Configuration file backups
3. Log file archives
4. Document all custom changes

## Support and Resources
- GitHub Issues: Report bugs and request features
- Documentation: Keep this guide updated
- Community: Join our Discord for support 
