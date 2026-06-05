package dev.stimmie.persistentblocks.listeners;

import dev.stimmie.persistentblocks.PersistentBlocks;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Iterator;
import java.util.List;

public class BlockProtectionListener implements Listener {

    private final PersistentBlocks plugin;

    public BlockProtectionListener(PersistentBlocks plugin) {
        this.plugin = plugin;
    }

    private NamespacedKey getBlockKey(Block block) {
        return new NamespacedKey(plugin, "block_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
    }

    private boolean hasCustomData(Block block) {
        return block.getChunk().getPersistentDataContainer().has(getBlockKey(block), PersistentDataType.BYTE_ARRAY);
    }

    private void dropCustomItemAndCleanUp(Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer chunkPDC = chunk.getPersistentDataContainer();
        NamespacedKey key = getBlockKey(block);

        byte[] itemBytes = chunkPDC.get(key, PersistentDataType.BYTE_ARRAY);
        chunkPDC.remove(key);

        if (itemBytes != null) {
            ItemStack customItem = ItemStack.deserializeBytes(itemBytes);
            if (customItem.getType() == block.getType()) {
                customItem.setAmount(1);
                block.getWorld().dropItemNaturally(block.getLocation(), customItem);
            }
        }
    }

    // 1. Prevent Pistons from moving custom blocks
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (hasCustomData(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (hasCustomData(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // 2. Handle Explosions (Creepers, TNT)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    private void handleExplosion(List<Block> blockList) {
        Iterator<Block> iterator = blockList.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (hasCustomData(block)) {
                dropCustomItemAndCleanUp(block);
                // Remove from vanilla explosion list so it doesn't drop a normal item too
                iterator.remove();
                // Manually set to air to simulate explosion
                block.setType(org.bukkit.Material.AIR);
            }
        }
    }

    // 3. Prevent Gravity & Endermen
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (hasCustomData(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // 4. Handle Fire
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (hasCustomData(event.getBlock())) {
            dropCustomItemAndCleanUp(event.getBlock());
        }
    }

    // 5. Handle Physics Breaks & Water Washing Away (Torches, Carpets, etc.)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (hasCustomData(event.getBlockState().getBlock())) {
            dropCustomItemAndCleanUp(event.getBlockState().getBlock());
            event.getItems().clear(); // Clear the vanilla items from dropping
        }
    }
}
