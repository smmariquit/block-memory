<h1 align="center">BlockMemory</h1>
<h4 align="center">Stop Losing Custom Items When Placed!</h4>

## 🔗 Important Links
* **Source Code:** [GitHub Repository](https://github.com/smmariquit/block-memory)
* **Documentation:** [GitHub Wiki](https://github.com/smmariquit/block-memory/wiki)
* **Bug Reports:** [Issue Tracker](https://github.com/smmariquit/block-memory/issues)

## 📖 Overview
In vanilla Minecraft, placing an item converts its `ItemStack` into a `BlockState`, which strips all custom NBT data. This causes issues for servers that give out custom-named or enchanted blocks as event rewards, as players lose the item data if accidentally placed. 

**BlockMemory** intercepts this process. It serializes the custom item and stores the byte array in the Chunk's `PersistentDataContainer` (PDC). When the block is broken, it deserializes the bytes and drops the original custom item.

## ⚙️ Dependencies
* **Required Server Software:** Paper 1.20 or higher (Folia is supported). *This plugin will NOT work on standard Spigot because it uses Paper's modern chunk data APIs.*
* **Required Java Version:** Java 21
* **Required Plugins:** None! It works out of the box.

## 📥 How to Install
1. Download the latest `BlockMemory-[version].jar` from the releases page.
2. Drop the file into your server's `plugins/` folder.
3. Restart your server.
4. That's it! There are no commands, permissions, or configuration files to mess with.

## 🛡️ Technical Features
* **No Databases or YAML Storage:** Data is stored natively within the chunk `.mca` files via the Paper API. This guarantees zero database overhead, prevents desyncs during chunk rollbacks, and ensures data is automatically purged if a chunk is deleted or reset.
* **full Edge-Case Handling:** The plugin is designed to prevent item duplication and orphaned data across vanilla mechanics.
 * **Explosions:** Intercepts `EntityExplodeEvent` and `BlockExplodeEvent` to drop the custom item and clean up PDC data.
 * **Pistons:** Cancels `BlockPistonExtendEvent` and `BlockPistonRetractEvent` for blocks containing custom PDC data to prevent coordinate desyncs.
 * **Gravity & Entity Interaction:** Cancels `EntityChangeBlockEvent` to prevent Endermen theft and to stop custom sand/anvils from falling and losing their coordinate mapping.
 * **Physics & Fluids:** Hooks into `BlockDropItemEvent` to securely drop custom items if a block is destroyed by water or if its supporting block is broken (e.g., torches).
* **Anti-Farming:** Automatically zeroes out the vanilla experience drop (`setExpToDrop(0)`) when a custom ore is mined, preventing players from infinitely farming XP by placing and mining the same block.
