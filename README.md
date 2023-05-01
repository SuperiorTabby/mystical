# Mystical
Mystical is a server-side fabric mod, meant to spice up gameplay. (It will still run on your machine, you don't need a remote server)
As you play, a mysterious force casts spells over the world, changing game mechanics and adding new challenges. Think of those chaos mods that people stream on twitch, but more sensible. It won't destroy your fun or your world in a few hours.

Mystical is **highly configurable**, **open-source**, and **awesome**.

Mystical is currently in beta - things are likely to change, and more polish and documentation is yet to come. Please feel free to ask questions or contribute to the project on [GitHub](https://github.com/skycatminepokie/mystical). Known issues, bugs, and future plans are also on GitHub.

### Warnings/known bugs
These will be fixed before long, but are important to here.
- Singleplayer mode is a bit broken - spells will work across worlds
- Major updates (the x in version x.y.z) indicate BREAKING changes - your game WILL CRASH if proper steps aren't taken when updating. I plan on resolving these as well.

| Command                          | Effect                                       | Permission                               | Default requirement |
|----------------------------------|----------------------------------------------|------------------------------------------|---------------------|
| `/mystical`                      |                                              | `mystical.command.mystical`              | None                |
| `/mystical spell`                |                                              | `mystical.command.spell`                 | None                |
| `/mystical spell new`            | Adds a new random spell                      | `mystical.command.spell.new`             | OP Level 4          |
| `/mystical spell new <spell>`    | Adds a new spell                             | `mystical.command.spell.new`             | OP Level 4          |
| `/mystical spell list`           | Shows all active spells                      | `mystical.command.mystical.spell.list`   | None                |
| `/mystical spell delete`         | Shows all active spells with a delete button | `mystical.command.mystical.spell.delete` | Op Level 4          |
| `/mystical spell delete <spell>` | Deletes a spell                              | `mystical.command.mystical.spell.delete` | Op Level 4          |
| `/mystical spell reload`         | Reloads spells from file                     | `mystical.command.mystical.reload`       | Op Level 4          |
| `/mystical haven`                | Attempts to haven the current chunk          | `mystical.command.mystical.haven.haven`  | None                |


Requires OwO-lib and Fabric API

#### Can I use this in a modpack?
Yes! Just don't rehost it without permission. And if you feel like it, let me know - I'm excited to see where this project goes!