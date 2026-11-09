# RaftOcean — Paper 1.21.11 plugin project

This is the **source project**, not a compiled installable JAR. The included GitHub Actions workflow builds the JAR online, which can be started from an Android browser.

## Features

- Creates a separate world named `RaftOcean`.
- Keeps vanilla-generated underground terrain, caves and ores below Y=30; converts the surface above it into a continuous ocean with a gravel seabed at Y=30 and water up to Y=62.
- Builds a 3×3 oak-plank raft at world spawn (0, 63, 0).
- Gives each player a fishing rod and one oak sapling the first time they join.
- Sends players joining from another world to RaftOcean and respawns them on the raft.
- Spawns 4–7 supply barrels per Minecraft day, initially at least 20 blocks from the raft. Barrels contain randomized wood, sticks, string, coal, dirt, seeds, saplings, iron nuggets, occasional iron ingots and very rare diamonds.
- Some crates have a loose resource item beside them with a small drifting velocity.
- Remembers the last supply day so a restart does not normally create a duplicate daily batch.

**Visual limitation:** barrels are placed at the ocean surface; the barrel blocks themselves do not animate or drift. Loose resource item entities can drift slightly.

## Build the installable JAR from Android using GitHub Actions

1. Sign in at https://github.com/ and create a repository named `RaftOcean`.
2. Upload the **contents** of this folder to the repository root. `pom.xml`, `src/`, and `.github/workflows/build.yml` must be at the root, not inside an extra nested folder.
3. Open the repository's **Actions** tab and enable Actions if prompted.
4. Select **Build RaftOcean plugin** from the left side, then tap **Run workflow**.
5. Wait for the workflow to finish with a green check mark. Open that run and find **Artifacts**.
6. Download `RaftOcean-Paper-1.21.11` and extract it. The installable file inside is `raft-ocean-1.0.0.jar`.

## Install on the hosting panel

1. Confirm server software is **Paper 1.21.11** and Java is **21**.
2. Stop the server and back up your existing files/worlds.
3. Upload `raft-ocean-1.0.0.jar` into the server's `plugins` folder.
4. Start the server and check the console for `RaftOcean enabled`.
5. The plugin creates a separate `RaftOcean` world automatically; your existing worlds are not overwritten.

## Notes

- The plugin creates a separate world named `RaftOcean`; it does not overwrite the existing main world.
- Chunks in a previously tested RaftOcean world are not automatically reset. To regenerate after a test, stop the server, back up, and delete only the `RaftOcean` folder.
- Existing players receive the starter items once, on their first join after installing the plugin.
