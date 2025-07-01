# Network Configuration for GitHub Actions
# This file documents the network requirements for building the saysth Minecraft mod

## Required Domains for GitHub Actions Firewall Allow List

### Essential Maven Repositories
- `maven.fabricmc.net` - Fabric mod loader dependencies
- `maven.architectury.dev` - Architectury API dependencies  
- `maven.neoforged.net` - NeoForge dependencies
- `repo1.maven.org` - Maven Central repository

### Fabric Ecosystem
- `fabricmc.net` - Fabric main site and documentation

### Gradle Build System
- `plugins.gradle.org` - Gradle plugins repository
- `services.gradle.org` - Gradle services and daemon communication

### Minecraft/Mojang
- `piston-meta.mojang.com` - Minecraft version metadata
- `piston-data.mojang.com` - Minecraft game data and assets
- `libraries.minecraft.net` - Minecraft runtime libraries

### Additional Dependencies
- `repo.spongepowered.org` - Sponge/Mixin framework dependencies
- `oss.sonatype.org` - Sonatype OSS repository snapshots

## GitHub Actions Setup

To configure these domains in GitHub Actions:

1. **Organization Level**: Add these domains to your organization's firewall allow list
2. **Repository Level**: Use the provided `.github/firewall-config.yml` file
3. **Enterprise Level**: Contact your GitHub Enterprise administrator to add these domains

## Ports Required
- **HTTP**: Port 80
- **HTTPS**: Port 443

## Testing Connectivity

You can test if the firewall configuration is working by adding a connectivity test step to your workflow:

```yaml
- name: Test Maven Repository Connectivity
  run: |
    curl -f https://maven.fabricmc.net/ || echo "Fabric Maven access blocked"
    curl -f https://maven.neoforged.net/ || echo "NeoForge Maven access blocked"
    curl -f https://repo1.maven.org/maven2/ || echo "Maven Central access blocked"
```