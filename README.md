# PersistentBlocks

A lightweight Paper Minecraft plugin that saves custom item data (enchantments, names, lore, and custom NBT) when blocks are placed in the world, and restores the data when the block is broken!

## Features
- **Zero Config, Plug and Play:** It just works out of the box.
- **Native Paper API:** Uses Paper's `serializeAsBytes()` and the `PersistentDataContainer` (PDC) on Chunks.
- **No External Databases:** Survives restarts and chunk unloading cleanly without bloating an SQLite file.

## How to Build
This project uses the `paperweight-userdev` Gradle plugin.
Since it targets Paper 1.20+, ensure you are using **Java 21**.

1. Open this folder in IntelliJ IDEA (or your preferred IDE).
2. Let the IDE import the Gradle project.
3. Run `gradlew build` or `gradlew reobfJar`.
4. To test a live server immediately, run `gradlew runServer` (provided by run-paper plugin).

## Why this is built differently
Instead of creating a massive database to track coordinates, this plugin saves the block's data directly inside the **Chunk** where it was placed using Paper's Persistent Data Container. When the chunk is loaded/unloaded, the data naturally follows it. If the chunk is ever deleted, the data is deleted too—meaning zero data bloat!
