# SpawnGuard

SpawnGuard is a lightweight and powerful spawn protection plugin for Paper servers.

It provides configurable spawn radius protection, build permissions, and PvP control — all managed through simple commands.

---

## Features

- Configurable spawn protection radius
- Block place & break prevention
- Prevents door, container, and interaction abuse
- Spawn-only PvP toggle
- Add/remove build permissions at spawn
- Live radius adjustment via command
- Lightweight and performance-friendly
- No external dependencies

---

## Commands

| Command | Description |
|----------|------------|
| `/spawnperms add <player>` | Allow a player to build at spawn |
| `/spawnperms remove <player>` | Remove build permission |
| `/spawnperms list` | List allowed players |
| `/spawnradius <number>` | Set spawn protection radius |
| `/spawnpvp on/off` | Enable or disable PvP in spawn |

---

## Configuration

```yaml
radius: 0
world: world
pvp_enabled: false
allowed_players: []
