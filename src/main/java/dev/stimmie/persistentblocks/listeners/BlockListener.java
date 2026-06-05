package dev.stimmie.persistentblocks.listeners;

import dev.stimmie.persistentblocks.PersistentBlocks;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockListener implements Listener {

    private final PersistentBlocks plugin;

    public BlockListener(PersistentBlocks plugin) {
        this.plugin = plugin;
    }

    // Helper to generate a unique key for the block within its chunk
    private NamespacedKey getBlockKey(Block block) {
        // x, y, z are absolute, which is fine since they are unique globally.
        // We could use relative-to-chunk coords, but absolute is easier and just as unique.
        return new NamespacedKey(plugin, "block_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        
        // We only care if the item has custom data (lore, enchants, custom name, etc.)
        if (!itemInHand.hasItemMeta()) {
            return;
        }

        // Check if there's actually anything special about the item meta
        // (If it's just a regular dirt block, we don't need to save it)
        boolean hasCustomData = itemInHand.getItemMeta().hasDisplayName() ||
                                itemInHand.getItemMeta().hasLore() ||
                                itemInHand.getItemMeta().hasEnchants() ||
                                !itemInHand.getItemMeta().getPersistentDataContainer().isEmpty();

        if (!hasCustomData) {
            return;
        }

        Block block = event.getBlockPlaced();
        Chunk chunk = block.getChunk();
        PersistentDataContainer chunkPDC = chunk.getPersistentDataContainer();
        NamespacedKey key = getBlockKey(block);

        // Serialize the item stack into a byte array
        // Note: serializeAsBytes() is a Paper-specific API, which is why we use Paper!
        byte[] itemBytes = itemInHand.serializeAsBytes();

        // Save it to the Chunk's PDC
        chunkPDC.set(key, PersistentDataType.BYTE_ARRAY, itemBytes);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        PersistentDataContainer chunkPDC = chunk.getPersistentDataContainer();
        NamespacedKey key = getBlockKey(block);

        // Check if this block has saved data
        if (!chunkPDC.has(key, PersistentDataType.BYTE_ARRAY)) {
            return;
        }

        byte[] itemBytes = chunkPDC.get(key, PersistentDataType.BYTE_ARRAY);
        
        // Always remove the data once the block is broken to prevent duplication/bloat
        chunkPDC.remove(key);

        if (itemBytes == null) return;

        // If in creative mode, don't drop the item (vanilla behavior)
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Deserialize back into an ItemStack
        ItemStack customItem = ItemStack.deserializeBytes(itemBytes);
        
        // EXPLOIT PREVENTION: 
        // If the plugin was uninstalled, and someone broke the block, the data would remain orphaned in the chunk.
        // If they later placed a normal dirt block here and broke it, it would drop the old enchanted item!
        // To fix this, we verify the saved item's material actually matches the block they are breaking.
        if (customItem.getType() != block.getType()) {
            return; // The block changed while the plugin was offline. Just drop vanilla and leave the data deleted.
        }

        // Ensure it only drops 1 amount (in case they placed from a stack of 64)
        customItem.setAmount(1);

        // Cancel vanilla drops and XP so players can't farm infinite XP by placing/mining custom ores
        event.setDropItems(false);
        event.setExpToDrop(0);

        // Drop our custom item
        block.getWorld().dropItemNaturally(block.getLocation(), customItem);
    }
}
