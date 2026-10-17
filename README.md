# Shared Vitality

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.6%20%28Fabric%29-6E8B3D?style=for-the-badge&logo=minecraft&logoColor=white" alt="Minecraft 1.21.6 Fabric" />
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/License-CC0%201.0-000000?style=for-the-badge" alt="License CC0 1.0" />
  <img src="https://img.shields.io/badge/Mod%20Status-Active-4CAF50?style=for-the-badge" alt="Active" />
</p>

<p align="center">
  <strong>Shared Vitality</strong> is a Fabric Minecraft mod built around the idea of a team-based survival experience where allies are linked by one shared life force.
</p>

## Overview

Shared Vitality turns a group of players into a single living organism. Instead of each player living separately, the party shares a common health pool, fights as a unit, and must manage distance, coordination, and roles to survive together.

This creates a co-op survival loop where:

- every team member contributes to one shared pool of vitality
- the distance between players matters and can affect the team
- roles define tactical advantages and weaknesses
- the whole squad must work together to survive harsh worlds and dangerous encounters

## Core mechanics

### Shared health pool

The team shares a single virtual health reserve, often called the Bloodlink Pool.

- damage taken by any ally reduces the common pool
- the same damage is mirrored and prevented from creating infinite loops
- if the pool reaches zero, the team is in danger of losing the run

### Tether / connection system

The team is linked by a shared tether, and the distance between allies affects game balance.

- close range: normal gameplay
- medium range: tension and penalties begin
- long range: debuffs, slow regeneration, and connection loss risk
- cross-dimension travel: extra pressure and decay effects appear

### Class system

Each player can receive one role at the start of the match. Roles add unique advantages and trade-offs so every squad can build different strategies.

Examples of roles include:

- Guardian
- Apothecary
- Delver
- Scout
- Reaper
- Enchanter
- Mason
- Beastmaster
- Shadow
- Soulbinder

### Start flow and progression

The experience is designed around a structured team setup:

1. players join in a waiting phase
2. an admin or host starts the round
3. each player receives a unique role
4. the team begins shared survival gameplay
5. players coordinate around the common pool and tether rules

## Featured item

The project includes a dedicated Bloodcloth / Blood Bandage style item designed to support recovery and team sustainability.

- crafted from simple materials
- helps restore vitality to the shared pool
- encourages coordinated team play instead of isolated selfish survival

## Requirements

- Minecraft: 26.2
- Mod loader: Fabric
- Java: 25
- Fabric API and required dependencies from the project build

## Installation

1. Download the latest release from the Releases page.
2. Put the mod file into your Fabric mods folder.
3. Launch the game using Fabric.
4. Start or join a world with the mod enabled.

## Build from source

This project uses Gradle.

```bash
git clone https://github.com/zhenya1337u/Shared-Vitality.git
cd Shared-Vitality
./gradlew build
```

If needed, you can also run the project in a local Fabric environment after building the jar.

## Project structure

```text
Shared-Vitality/
├── src/
│   ├── client/
│   └── main/
│       ├── java/net/sharedvitality/
│       │   ├── compat/
│       │   ├── config/
│       │   ├── item/
│       │   ├── network/
│       │   ├── pool/
│       │   ├── role/
│       │   └── SharedVitalityMod.java
│       └── resources/
├── build.gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
├── gradle.properties
├── LICENSE
├── README.md
└── idea.md
```

## Configuration

The project contains configuration logic to adjust gameplay balance and parameters such as:

- shared pool scaling
- tether distance thresholds
- cross-dimension decay behavior
- friendly-fire handling
- team survival tuning

## Why this mod is interesting

Shared Vitality is not just a damage modifier or a small balance pack. It creates a full team survival identity:

- every player matters as part of a group
- strategy is based on communication and positioning
- role selection matters deeply
- the team must survive as one body, not as separate entities

## Contributing

Contributions are welcome.

If you want to help:

1. fork the repository
2. create a feature branch
3. make your changes
4. open a pull request with a clear description

## License

This project is licensed under the Creative Commons Zero v1.0 Universal license.

See the LICENSE file for details.

## Links

- Repository: https://github.com/zhenya1337u/Shared-Vitality
- Releases: https://github.com/zhenya1337u/Shared-Vitality/releases

<p align="center">
  <sub>Built for cooperative survival and shared risk.</sub>
</p>

