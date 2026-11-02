# OpenBlocks Elevator — Fabric 1.20.1 port

Unofficial port of [VsnGamer's ElevatorMod](https://github.com/VsnGamer/ElevatorMod) (the OpenBlocks elevator) to **Fabric 1.20.1**, based on its `1.20.1-forge` branch.
All credit for the mod goes to the original authors. MIT licensed, like the original (see `LICENCE`).

Port no oficial a **Fabric 1.20.1** del Elevator Mod de VsnGamer.

## Features
- Elevator blocks in 16 colours: jump to go up, sneak to go down.
- Camouflage with any block, directional arrow and optional facing on teleport (GUI).
- Config in `config/elevatorid.json`.

## Requirements
Minecraft 1.20.1, Fabric Loader ≥ 0.14, Fabric API.

## Changes from the Forge version
- Forge registries, networking and config replaced with Fabric API equivalents.
- Camouflage rendering uses the Fabric Rendering API (FRAPI).
- Light emission from the camouflage block is stored in the block state.

## Building
`./build.sh` (Docker, JDK 25, Loom 1.18). The jar ends up in `build/libs/`.
