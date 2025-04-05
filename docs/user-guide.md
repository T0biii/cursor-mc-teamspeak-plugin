# Minecraft-TeamSpeak Integration Plugin - User Guide

## Introduction
This guide will help you understand how to use the Minecraft-TeamSpeak Integration Plugin as a regular user. This plugin allows you to link your Minecraft account with your TeamSpeak account and enjoy various features.

## Features
- Link your Minecraft account with TeamSpeak
- View online TeamSpeak users in-game
- Automatic role synchronization
- Real-time status updates

## Getting Started

### Prerequisites
- A Minecraft server running the plugin
- A TeamSpeak 3 client
- Access to the TeamSpeak server

### Basic Commands

#### Linking Your Account
To link your Minecraft account with TeamSpeak:
```
/ts link <your-teamspeak-username>
```
This will generate a verification code that you need to enter in TeamSpeak.

#### Unlinking Your Account
To unlink your Minecraft account from TeamSpeak:
```
/ts unlink
```

#### Viewing Online Users
To see who's currently online on TeamSpeak:
```
/ts list
```

#### Getting Help
To view all available commands and their descriptions:
```
/ts help
```

## Common Issues and Solutions

### Account Linking Issues
1. **Verification Code Not Working**
   - Make sure you're using the correct TeamSpeak username
   - Try unlinking and linking again
   - Contact an administrator if the problem persists

2. **Already Linked Account**
   - Use `/ts unlink` first before linking a new account
   - If you can't unlink, contact an administrator

### TeamSpeak Connection Issues
1. **Cannot See Online Users**
   - Check if the TeamSpeak server is online
   - Wait a few minutes and try again
   - Use `/ts list` to refresh the user list

## Best Practices
1. Always use your exact TeamSpeak username when linking
2. Keep your TeamSpeak client updated
3. Report any issues to server administrators
4. Don't share your verification codes with others

## Support
If you encounter any issues:
1. Check this guide for common solutions
2. Ask for help in the server's support channel
3. Contact a server administrator
4. Create an issue on the plugin's GitHub repository

## Updates
The plugin will automatically check for updates when you join the server. You'll be notified if a new version is available. 
