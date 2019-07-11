package me.xiione;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

/**
 * A Utility class with static methods to quickly manage metadata.
 */
public class MetaDataManager {

    /**
     * Applies a cooldown time to an entity.
     * @param entity
     * @param type
     * @param milliseconds
     * @param plugin
     */
    public static void setCoolDown(Entity entity, String type, Long milliseconds, Plugin plugin) {
        entity.setMetadata(type, new FixedMetadataValue(plugin, System.currentTimeMillis() + milliseconds));
    }

    /**
     * Checks to see if an entity is on cooldown.
     * @param entity
     * @param cooldown
     * @return
     */
    public static boolean onCoolDown(Entity entity, String cooldown) {
        return (entity.hasMetadata(cooldown) && entity.getMetadata(cooldown).get(0).asLong() > System.currentTimeMillis());
    }

    /**
     * Checks to see if an entity is off cooldown and applies a new cooldown if they are.
     * @param entity
     * @param cooldown
     * @param milliseconds
     * @param plugin
     * @return
     */
    public static boolean refreshCoolDown(Entity entity, String cooldown, Long milliseconds, Plugin plugin) {
        if (!entity.hasMetadata(cooldown)) {
            setCoolDown(entity, cooldown, milliseconds, plugin);
            return true;
        }

        if (!onCoolDown(entity, cooldown)) {
            setCoolDown(entity, cooldown, milliseconds, plugin);
            return true;
        }

        return false;
    }
}
