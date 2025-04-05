# Contributing to Minecraft-TeamSpeak Integration

Thank you for your interest in contributing to the Minecraft-TeamSpeak Integration Plugin! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md) to maintain a respectful and inclusive community.

## How to Contribute

### Reporting Issues

1. Check if the issue already exists in the [Issues](https://github.com/minecraft-teamspeak/webui/issues) section
2. Create a new issue with a clear title and description
3. Include relevant information:
   - Minecraft server version
   - Plugin version
   - TeamSpeak server version
   - Steps to reproduce
   - Expected behavior
   - Actual behavior
   - Logs (if applicable)

### Pull Requests

1. Fork the repository
2. Create a new branch for your feature/fix
3. Make your changes
4. Write/update tests if necessary
5. Update documentation if needed
6. Submit a pull request

### Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/minecraft-teamspeak/webui.git
   cd webui
   ```

2. Install dependencies:
   ```bash
   cd minecraft-plugin
   mvn install
   ```

3. Set up your development environment:
   - Java 17 or higher
   - Maven
   - IDE (IntelliJ IDEA recommended)

### Coding Standards

- Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Add comments for complex logic
- Write unit tests for new features
- Keep commits atomic and well-described

### Documentation

- Update documentation for any new features
- Follow the existing documentation style
- Include code examples where appropriate
- Update the changelog for significant changes

## Building the Project

### Local Build

```bash
cd minecraft-plugin
mvn clean package
```

### Docker Build

```bash
# Linux/macOS
./build.sh

# Windows
build.bat
```

## Release Process

1. Update version numbers in:
   - `pom.xml`
   - `plugin.yml`
   - `CHANGELOG.md`
2. Create a new release tag
3. Build and test the release
4. Create a GitHub release
5. Update documentation if needed

## Getting Help

- Join our [Discord server](https://discord.gg/minecraft-teamspeak)
- Check the [documentation](https://minecraft-teamspeak.github.io/webui/)
- Ask questions in GitHub Discussions

## License

By contributing to this project, you agree that your contributions will be licensed under the project's [MIT License](LICENSE). 
