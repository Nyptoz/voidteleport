# VoidTeleport
A simple and lightweight Spigot plugin for lobby worlds and servers by **Metall_King**.

## Features
### Teleportation Effects
The plugin features a robust set of flags in `/setspawn` and `/editspawn` to precisely specify spawn data:
- `-world <world>` - Specifies the world being configured.
- `-position <x y z / current>` - Specifies the spawn coordinates. Using `current` applies your current position rounded to the nearest block.
- `-rotation <pitch yaw / current / none>` - Specifies the spawn viewing angle. Using `current` applies your current looking direction rounded to integers. Using `none` prevents the player's rotation from being modified upon teleportation. 
- `-height <height>` - Specifies the Y-coordinate threshold at which players will instantly teleport.
- `-message <message> <prefix>` - Specifies the chat message sent to the player upon teleportation. Supports standard `&` color codes. The `prefix` argument toggles whether the global prefix from `messages.yml` is prepended.
- `-sound <sound> <optional:volume> <optional:pitch>` - Specifies the sound played upon teleportation using the standard Spigot Sound registry.
- `-particle <particle> <optional:count> <optional:force> <optional:data>` - Specifies the effect particles spawned upon teleportation using the Spigot Particle registry. Includes full smart Tab-Completion support for complex dust options (e.g., `DUST`, `DUST_COLOR_TRANSITION`).

---

## Commands
An intuitive spawn management system manageable via in-game commands or straight from the server console:
- `/voidteleport <command>` (Alias: `/vt <command>`) - Master base command. Running this without sub-commands displays the plugin's **Version Info** (the same output can be triggered via `/vt info`).
- `/spawn <optional:world>` - Teleports you to the defined spawnpoint of the target or current world, executing all configured triggers and effects.
- `/spawninfo <optional:world>` - Prints a beautifully formatted summary of all active Spawn Data for the specified or current world.
- `/setspawn <optional:FLAGS>` - Generates a new spawn profile. Automatically defaults to your current world, coordinates, and rotation if no override flags are passed.
- `/editspawn <optional:FLAGS>` - Modifies an existing spawn profile. Defaults to your current world and updates *only* the specific flags you type out.
- `/reloadspawn <optional:world>` - Reloads spawn properties for the given or current world directly from the `config.yml` file.
- `/delspawn <optional:world>` - Permanently deletes all active spawn settings for the specified or current world.
- `/setheight <optional:-world world> <optional:height>` - Instantly updates the falling trigger height limit to your current Y-level or a specified coordinate.

---

## Installation
### Requirements
- Java SE 8 or higher installed on your system.
- A [Spigot](https://www.spigotmc.org/) or [PaperMC](https://papermc.io/) server executable above Minecraft 1.13. 
> ⚠️ **WARNING:** This plugin relies directly on core Spigot mappings and will not function on legacy, un-forked vanilla CraftBukkit.

### Step-by-Step Guide
1. Head over to the [Releases Tab](https://github.com/Nyptoz/voidteleport/releases) and download the latest compilation build.
2. Drop the `VoidTeleport-x.x.x.jar` file directly into your server's `/plugins/` directory.
3. Load the plugin into memory by restarting your server, or hot-load it dynamically using tools like [PlugManX](https://modrinth.com/plugin/plugmanx) via `/plm load VoidTeleport`.

---

## Configuration Files
Upon initial startup, the plugin automatically generates a dedicated asset folder at `/plugins/VoidTeleport/`. It generates the following default configurations:

#### config.yml
```yaml
show-no-permissions: true

preview-sounds: true
preview-particles: true

particle-delay: 1

worlds:
  lobby:
    teleportHeight: 0.0
    spawnLocation:
      x: 1.5
      y: 64.0
      z: -3.5
      pitch: 0.0
      yaw: 0.0
    
    sound:
      id: BLOCK.AMETHYST_BLOCK.STEP
      volume: 1.0
      pitch: 0.75
    
    particle:
      id: DUST_COLOR_TRANSITION
      count: 64
      speed: 1.0
      data: 0 170 170 85 255 255 1.25
    
    message: '&eYou fell into the void the &llobby'
    messagePrefix: true
```

#### messages.yml
```yaml
use-prefix: true
prefix: '&b&lVoid&3&lTeleport &8» '

use-sounds: true

success-sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
fail-sound: "BLOCK_NOTE_BLOCK_DIDGERIDOO"

messages:
  no-permission: "&cYou don't have permission to execute this command!"
  only-players: "&cPlease &4specify World Names &cin Terminal"

  world-not-found: "&cCouldn't find the World &4%world%"
  spawn-not-found: "&cCouldn't find a Spawn Data for &4%world%"

  all-reloaded: "&fAll &7Config Files have been reloaded"
  world-reloaded: "&f%world%'s Spawn Data &7has been reloaded"
  messages-reloaded: "&fMessages Config &7has been reloaded"

  parse-error: "&cThere was an error &4parsing &cthe coordinates!"

  spawn-set: "&7Spawn has been set for &f%world%"
  spawn-deleted: "&7Deleted all Spawn Data for &f%world%"
  spawn-edited: "&7Edited &f%world%'s Spawn"

  height-set: "&7Height has been set to &f%height% &7for &f%world%"
  height-below-spawn: "&cHeight wasn't changed due to it being &4above &cSpawn Position"

  force-teleport: "&7Teleported you to &f%world%'s Spawn"

  spawn-info:
    - ''
    - '&8&m-          -&r &b%world%''s Spawn Data &8&m-           -'
    - '&8• &7Limit Height: &f%height%'
    - '&8• &7Coordinates: &f%x% %y% %z%'
    - '&8• &7Sound: &f%sound%'
    - '<req:%sound%>  &8• &7Volume: &f%volume%%%'
    - '<req:%sound%>  &8• &7Pitch: &f%pitch%%%'
    - '&8• &7Particle: &f%particle%'
    - '<req:%particle%>  &8• &7Extra Properties: &f%particleData%'
    - '&8&m-                                                  -'
    - ''

  plugin-info:
    - ''
    - '&8&m-                   -&r &fInfo &8&m-                   -'
    - '         &b&lVoid&3&lTeleport &r&7- &f%version%'
    - '   &7A simple Plugin by &eMetall_King&r&7.'
    - '&8&m-                                                -'
    - ''
```

---

## Support
See the Plugin Wiki for comprehensive API details, or join our official Discord Server to ask questions, check development builds, or report bugs.
