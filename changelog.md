
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
