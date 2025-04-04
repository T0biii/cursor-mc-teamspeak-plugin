# Minecraft-TeamSpeak Integration Plugin

This project provides integration between Minecraft and TeamSpeak 3 servers, allowing for:
- Real-time TeamSpeak user list synchronization
- Minecraft-TeamSpeak account linking
- Automatic TeamSpeak role management

## Components

1. Minecraft Plugin (Java)
2. Database (PostgreSQL or SQLite)
3. TeamSpeak 3 Server
4. Docker Compose setup for all services

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose (optional, for PostgreSQL setup)
- TeamSpeak 3 Server
- PostgreSQL Database (optional, SQLite is available as an alternative)

## Project Structure

```
.
├── docker-compose.yml
├── minecraft-plugin/
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── database/
│   └── init.sql
└── README.md
```

## Setup Instructions

1. Clone this repository
2. Configure the TeamSpeak 3 server settings in `config.yml`
3. Choose your database type:
   - **PostgreSQL**: Use the provided Docker Compose setup
   - **SQLite**: No additional setup required, the plugin will create the database file automatically
4. Start the services:
   - If using PostgreSQL: `docker-compose up -d`
   - If using SQLite: Just start your Minecraft server with the plugin installed
5. The Minecraft server will be available at `localhost:25565`
6. If using PostgreSQL, the database will be available at `localhost:5432`

## Database Options

### PostgreSQL
- More robust for larger servers
- Better performance for high concurrency
- Requires a separate database server
- Configure in `config.yml` under `database.type: "postgres"`

### SQLite
- Simple file-based database
- No additional server required
- Perfect for smaller servers
- Configure in `config.yml` under `database.type: "sqlite"`

## Features

- Asynchronous TeamSpeak user list caching
- Automatic role management based on Minecraft ranks
- Account linking system
- Real-time updates
- Database persistence
- Multiple database backend options

## Configuration

See `config.yml` for detailed configuration options. Key settings include:

```yaml
database:
  type: "sqlite"  # Options: "postgres" or "sqlite"
  # PostgreSQL settings (only used if type is "postgres")
  postgres:
    host: "postgres"
    port: 5432
    database: "minecraft_teamspeak"
    username: "minecraft"
    password: "minecraft"
  # SQLite settings (only used if type is "sqlite")
  sqlite:
    file: "plugins/TeamSpeakIntegration/database.db"
```

## Implementation Todo List

### 1. Project Setup
- [x] Create project structure
- [x] Set up Maven configuration
- [x] Create plugin.yml
- [x] Create configuration file
- [x] Set up Docker environment

### 2. Database Implementation
- [x] Create database schema
- [x] Implement database connection pool
- [x] Create data access objects (DAOs)
- [x] Implement account linking storage
- [x] Implement user cache storage
- [x] Add SQLite support as an alternative to PostgreSQL

### 3. TeamSpeak Integration
- [x] Implement TeamSpeak Query API connection
- [x] Create user list caching system
- [x] Implement async update mechanism
- [x] Add error handling and reconnection logic
- [x] Implement role management system

### 4. Minecraft Plugin Features
- [x] Create main plugin class
- [x] Implement command handlers
- [x] Add account linking system
- [ ] Create verification system
- [x] Implement role synchronization

### 5. Commands to Implement
- [x] `/ts link` - Link Minecraft account with TeamSpeak
- [x] `/ts unlink` - Unlink Minecraft account from TeamSpeak
- [x] `/ts list` - Show online TeamSpeak users
- [x] `/ts help` - Show help information
- [x] `/tsadmin reload` - Reload plugin configuration
- [x] `/tsadmin update` - Force update TeamSpeak user list
- [x] `/tsadmin status` - Show plugin status

### 6. Events to Handle
- [ ] Player join event - Check for linked account
- [ ] Player quit event - Update status
- [ ] TeamSpeak user join event
- [ ] TeamSpeak user leave event
- [ ] Rank change events

### 7. Testing
- [ ] Unit tests for database operations
- [ ] Integration tests for TeamSpeak connection
- [ ] Command testing
- [ ] Role synchronization testing
- [ ] Performance testing

### 8. Documentation
- [x] Create README.md
- [x] Add inline code documentation
- [ ] Create user guide
- [ ] Create admin guide
- [ ] Document API endpoints

### 9. Deployment
- [x] Create Docker Compose configuration
- [ ] Set up CI/CD pipeline
- [ ] Create backup strategy
- [ ] Create update mechanism
- [ ] Create monitoring system

### 10. Security Considerations
- [ ] Implement rate limiting
- [ ] Add input validation
- [ ] Secure database credentials
- [ ] Implement proper error handling
- [ ] Add logging system 
