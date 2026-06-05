package dev.stimmie.persistentblocks;

import dev.stimmie.persistentblocks.listeners.BlockListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class PersistentBlocks extends JavaPlugin {

    private static PersistentBlocks instance;
    private NamespacedKey itemDataKey;

    @Override
    public void onEnable() {
        instance = this;
        // Key used to store the serialized item data in the block's PDC
        this.itemDataKey = new NamespacedKey(this, "saved_item_data");

        // Register event listeners
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(this), this);

        getLogger().info("PersistentBlocks has been enabled! Custom block NBT will now be saved.");
    }

    @Override
    public void onDisable() {
        getLogger().info("PersistentBlocks has been disabled.");
    }

    public static PersistentBlocks getInstance() {
        return instance;
    }

    public NamespacedKey getItemDataKey() {
        return itemDataKey;
    }
}
