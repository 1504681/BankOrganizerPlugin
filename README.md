# Debug Enemy List Plugin

A RuneLite plugin that displays all NPCs (enemies) on screen with their tile coordinates.

## Features
- Shows up to 15 NPCs in an overlay panel
- Displays NPC name and world coordinates [X, Y]
- Updates in real-time as you move around

## Installation

1. Make sure you have Java 11 installed
2. Run `build-and-install.bat`
3. Restart RuneLite
4. Enable "Debug Enemy List" in the plugin configuration

## Building from source

```bash
gradlew build
```

The plugin JAR will be created in `build/libs/`