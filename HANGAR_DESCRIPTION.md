# BlockMemory
*Stop Losing Custom Items When Placed!*

## Overview
In vanilla Minecraft, placing an item converts its `ItemStack` into a `BlockState`, which strips all custom NBT data. This causes issues for servers that give out custom-named or enchanted blocks as event rewards, as players lose the item data if accidentally placed. 

**BlockMemory** intercepts this process. It serializes the custom item and stores the byte array in the Chunk's `PersistentDataContainer` (PDC). When the block is broken, it deserializes the bytes and drops the original custom item.

## Technical Features
*   **No Databases or YAML Storage:** Data is stored natively within the chunk `.mca` files via the Paper API. This guarantees zero database overhead, prevents desyncs during chunk rollbacks, and ensures data is automatically purged if a chunk is deleted or reset.
*   **Comprehensive Edge-Case Handling:** The plugin is designed to prevent item duplication and orphaned data across vanilla mechanics.
    *   **Explosions:** Intercepts `EntityExplodeEvent` and `BlockExplodeEvent` to drop the custom item and clean up PDC data.
    *   **Pistons:** Cancels `BlockPistonExtendEvent` and `BlockPistonRetractEvent` for blocks containing custom PDC data to prevent coordinate desyncs.
    *   **Gravity & Entity Interaction:** Cancels `EntityChangeBlockEvent` to prevent Endermen theft and to stop custom sand/anvils from falling and losing their coordinate mapping.
    *   **Physics & Fluids:** Hooks into `BlockDropItemEvent` to securely drop custom items if a block is destroyed by water or if its supporting block is broken (e.g., torches).
*   **Anti-Farming:** Automatically zeroes out the vanilla experience drop (`setExpToDrop(0)`) when a custom ore is mined, preventing players from infinitely farming XP by placing and mining the same block.

## Requirements
*   **Paper 1.20+** *(Requires Paper API for `serializeAsBytes` and chunk PDC support. Will not run on standard Spigot).*
*   **Java 21**

## Installation
Place the `.jar` in your `plugins/` folder and restart the server. There are no configuration files, commands, or permissions required.
