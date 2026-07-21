
### Version 2.7.9:
- Reworded the "auto-reloaded music player" toast to make it clearer that the auto-reload is actually successful.
- Fixed music player crash when main game thread hangs for a while.


### Version 2.7.8:
- Fixed music player crash with FLAC files where no metadata is present.
- Fixed bug that causes the game to freeze when combat is registered.


### Version 2.7.7:

- Fixed crash when joining server with other players online in some versions of Minecraft.
- Fixed rare mod-conflict and crash due to cross-threaded access to world entities.
  * The `@village`, `@ranch`, and `@warden_nearby` now only update once a second to reduce load on main game thread.
- Attempted to fix rare music player crash on startup in huge modpacks.
- Attempted fix at vanilla music and custom music overlapping.
- Fixed crash with sufficiently low gain on music file (e.g. `"..."<-50.0>`).
- The music player will now automatically attempt to restart once upon crash.


### Version 2.7.6:

- Fixed issue where combat tracking breaks when certain combat mods are installed (or at least Epic Fight and SlashBlade).
- Fixed issue where combat tracking generally did not work properly in multiplayer (while working in singleplayer).
- Fixed music player crashes related to MP3 file metadata parsing.
- Fixed problem where mods that constantly update the music volume cause the game rendering to hang for the duration of Ambience Mini's fade in/out effect when changing music.
  * Why constantly update music volume? No idea.
- Attempt at fixing weird null-pointer crash on startup in the mod's event handlers in Minecraft 1.21.1.


### Version 2.7.5:

- Fixed crash bugs when joining a server without Ambience Mini.
- Added mod-config option for showing a toast with music info whenever the music changes.
- Fixed music player crash when near a jukebox (or a cockroach with Maracas from Alex's mobs since the music is played like record music) in multiplayer affecting Minecraft versions 1.19.2 and above.
- Fixed music player crash with some OGG and MP3 files.


### Version 2.7.4:

- Fixed mixin refmap issue.


### Version 2.7.3:

- Fixed intermittent stuttering/glitches when playing MP3 files.
- Bugfixes and improvements in music players/decoders.


### Version 2.7.2:

- Fixed server-sided crash bug when attacking entities with projectiles.


### Version 2.7.1:

- Hopefully fixed bug where quickly entering and leaving a boss fight causes the music player to crash.
- Added `@is_targeted` event which is `true` when any mob is targeting the player (and `undefined` when there is no server-support).
- Added `@is_fighting` event which is `true` when any mob has had recent (damage-based) interaction with the player.
- The `@in_combat` event works roughly the same, activating only when actively fighting and staying active until the player is safe or sufficient time has passed.
  * You can now be in combat when in creative mode, but not targeted. You can use `$game_mode` if you do not want combat music in creative.
  * Removed `Leaving_Combat_Distance` mod config option in favor of time-based combat detection (and since long-distance combat should still count as combat).
- Fixed bug for Minecraft versions 1.19.2 and above that caused incorrect tracking of enemies in combat.
- Improved combat tracking in general.
- Improved error messaging in an edge case.
- Fixed brainfart where messages (errors, notifications, etc.) are created but not printed to the chat...


### Version 2.7.0:

- Improved structure caching and intersection checking.
- Added the ability to re-enable the vanilla music player in the music config.
  * Use the `use_vanilla_player` command (instead of a `play` command) to enable the vanilla player.
- Added a `$combatants` property of type `list<combatant>` using the new `combatant` type.
  * `combatant` has four fields (accessed using `combatant.field`): `type_id`, `health`, `maxHealth`, and `health_percent`.
  * This can be used to select music based on the type and state of fought mobs.
- Added fields `int.floatVal` and `float.intVal` for converting between whole and decimal numbers.
- Added arithmetic operators `+`, `-`, `*`, and `/` to the configuration language. 
- Added player events `@on_fire` and `@in_powder_snow`.
- Added `length` field to lists. `list.length` will get the length of a list.
- Added support for `loopstart` and `looplength/loopend` metadata tags for all supported music files.
  * This will cause the music to start out as normal, but continue looping some part after reaching the end of the loop interval.
  * `loopstart` and `looplength/loopend` are all measured in samples (the smallest piece of audio data).
- Mod now prints a warning to the logs when music directory contains unused music files.
- Mod now prints warnings about unused identifiers (e.g., playlists) in the music config.

****


### Version 2.6.2:

- Fixed a bug where `$flags[x]` would return `undefined` even if the flag `x` exists.


### Version 2.6.1:

- Fixed a bug where reading the `$elevation` property before the player has been loaded would cause the music player to crash.


### Version 2.6.0:

- You can now set priorities of selected playlists by writing `play PLAYLIST priority PRIORITY` where `PRIORITY` must be a non-negative, whole number given as a constant/literal.
  * When a playlist is selected using `play`, all music with a lower priority is paused, and when the high-priority music stops, the paused low-priority music resumes if conditions still allow playing this music at this point.
  * Interrupts now only affect `play` keywords *without* a manually specified priority and sets the priority to "the number of nested interrupts". Thus, "`interrupt play LIST`" will give `play LIST` a priority of `1` and "`interrupt interrupt play LIST`" gives a priority of `2`.
  * When no manual priorities are given in your music configuration, interrupts will work exactly as before, but without any nesting limitations.
- Added a [flag system](https://github.com/sdfg610/Ambience-Mini-Mod/wiki/Flags) where named variables can be set to string values.
  * Use `/ambience_mini flag create/update [name] [value]` to create/update flags.
  * Use `/ambience_mini flag delete [name]` to remove flags.
  * The `$flags` property is a map from flag-names to flag-values. If you have created a flag named `test_flag` with the value `test value`, then `$flags["test_flag"] == "test value"` will evaluate to `true`.

****


### Version 2.5.1:

- Ambience Mini now uses Minecraft's built-in audio system to play music which should hopefully give a more Linux-friendly experience.
  * Removed the forge config for "Ignore master volume" due to the more direct integration with Minecraft's audio system.
  * To the best of my ability, I've tweaked soundtrack-gains (i.e. `<+5.0>` in `"music.mp3" <+5.0>`) to increase/decrease volume in a similar way to before, but the new volume system makes this a bit challenging.
- Lots of improvements to the music playing logic in connection with the new audio system and in preparation for upcoming features.
- Added support for `.ogg` music files.


### Version 2.5.0:

This is the biggest update to Ambience Mini yet which introduces a system for configuring named areas (or regions) which can be used for music selection. Staying true to the philosophy of "no server-support required to run", the area system works regardless of whether the server has the mod installed or not. If no server-support, the server-dependent features are simply disabled.

The configuration language has also been extended a lot to make it more usable and flexible, but while retaining the original simplicity. **Beware of breaking changes to the configuration language syntax as described below**. Finally new events and properties have been added as described below.

**Area system:**
- Added a client-side-based system for configuring areas.
  * Areas can always be stored locally (stored separately from world; sensitive to world and server names). 
  * With server support, areas can be stored within the world and optionally shared with other players.

**Events and Properties:**
- Added `$areas` property which contains a list of all areas the player is currently touching.
  * `$areas` is ordered by the volume of areas in increasing order (first area is the smallest).
- Added `$uuid` property for getting the id of the player. (Can be used to check area ownership.)
- Added `@warden_nearby` event which is active when a Warden is within 32 blocks (2 chunks) of the player.
  * **Note:** This event is only available from Minecraft 1.19 and up.
- Added `$structures` property which contains a list of the ids of all structures that the player is in.
    * **Note 1:** even if the "structure blocks" are removed, the "structure bounding box" still exists.
    * **Note 2:** it is a bit difficult to get a sensible hitbox for structures and some structures' hitboxes might be a slightly off. If so, feel free to report where this happens.
    * **Important:** `$structures` requires server support to function. If no server-support, then `$structures == undefined`.

**Configuration language:**
- **Breaking change:** Changed the syntax for `all/any [x] in [list] **where** [condition] end` to `all/any [x] in [list] **has** [condition] end`
- Added indexing operator `[]` for lists where the first element has index `0`. Example: `list[0]`.
- All values in the configuration language can now have an "undefined" value.
  * For example, `list[n] == undefined` if there is no element at index `n` in the list.
  * Also, if no world is currently loaded, events such as "@rain" will also have `@rain == undefined`.
- Added the constant `undefined` which can be of any type.
- Added operator `[value] *~ [regex]` for matching strings with regular expressions (using Java regex syntax).
- Added more number-comparison operators: `<=`, `>`, and `>=`.
- Added unary operators for logical not `!` and for number negation `-`.
- Added field access notation `value.field`. 
  * As an example `area.name` gets the name of an area, whereas `area.owner` gets the owner's id (if the area is owned).
- Added `let [name] = [value] in [schedule]` for introducing named values in a schedule.
  * This allows the values of expensive events and properties for reuse in multiple checks.
- Fixed bug in list equality check where equality was only checked up to the length of the shortest list.

**Other:**
- Many big and small improvements and fixes.

****


### Version 2.4.4:

- Added "how to reload music player" message when music player crashes
- Fixed crash where Ambience Mini's modified copy of the FLAC library can cause a crash with other FLAC libraries.


### Version 2.4.3:

- Fixed crash related to jukebox music detection.
- Added keybinding to pause and resume music (default "End").


### Version 2.4.2:

- Added forge-config option "Fadeout on jukebox" which when set to true (the default) makes the Ambience Mini music fade out when a jukebox is playing nearby.
- Fixed crash when printing all state when Ambience Mini failed to load at startup and has not been reloaded.
- Stability improvements related to combat and game-mode tracking.


### Version 2.4.1:

- Fixed bug where certain ways of displaying boss names would cause the music engine to crash.


### Version 2.4.0:

- Changed cave detection algorithm to work in multiplayer even when Ambience Mini is not installed on the server.
- Added event "drowning".
- Added properties "game_mode" and "difficulty".
- Added property "skylight_potential" for determining average maximum skylight (assuming middle of the day) in the player's vicinity.
- Fixed a music engine crash when exiting a world.
- Fixed a game crash when pressing "select new music" when no music is playing.
- Fundamental restructuring of music engine to allow sharing code between the Minecraft mod and the Ambience Mini IDE.

****


### Version 2.3.6:

- Made a fix to ignore the fake player and world that are intermittently put in place of real player and world by the Essential mod for a very short moment, and which caused Ambience Mini to read incorrect player and world state at random.


### Version 2.3.5:

- Added language entry for "meticulous playlist selector" forge-config (only affects 1.21.1 and above).
- Verbose mode now prints more information.
- Potentially increased stability of game-state reader.


### Version 2.3.4:

- Added forge-config option (meticulous playlist selector) for double-checking playlist selections to counteract incorrect readings caused by mod-interactions.


### Version 2.3.3:

- Made a more robust system event and property reading in an attempt to fix incorrect behavior, likely due to mod-interactions.


### Version 2.3.2:

- Fixed crash-bug related to the less-than operator in music configurations.
- Other fixes and improvements to code structure.


### Version 2.3.1:

- Added a "verbose mode" forge-config option. When enabled, the values (of the events and properties) used to pick a playlist will be printed whenever the current playlist changes.
    * This information can be used when debugging music packs to see where odd behavior stems from.
- Fixed bug where the config option for "notify server support" has wrong name (be careful with that copy-pasta coding).


### Version 2.3.0:

- Added lists data types (separate from playlists) and list quantifiers (i.e., "all" and "any") to the configuration language.
- Added a "$biome_tags" (string list) property that lists all the tag names of the players current biome.
- Added an "$effects" (string list) property that lists all active effects/(de-)buffs on the player.
- Added a "$bosses" (string list) property listing all active bosses (since multiple bosses can be active at once).
    * The old "$boss" property still works and simply gets the first boss in the list (same always, actually).
- Replaced the "print cave score" keybinding with a "print all readings" keybinding that prints all game state to the Minecraft log.
- Fixed bug that caused fishing music to constantly reset when moving around too much while fishing or having a very short fishing timeout.

****


### Version 2.2.0:

- Added combat tracking to the music engine.
- The mod is now optional on both the client and server and just enables/disables features depending on where the mod is present.
- Improved the cave scoring algorithm through more fine-grained scoring of the environment.
- Various backend improvements.
- Changed the minimum sound volume such that the music is not way too silent even at 50% volume.
- Fixed a bug where the music player would crash when entering a boss fight (affected 1.19.2, 1.21.1, and 1.21.1).

****


### Version 2.1.1:

- Slight improvements to the cave-scoring system.
- Small backend-improvements and bug-fixes.
- Added support for Minecraft 1.21.1.


### Version 2.1.0:

- Added new some new events and properties. Especially the new cave scoring system.
- Fixed bug where the same music can play twice in a row.
- Quality-of-life improvements, bug-fixes, and many other behind-the-scenes things.
