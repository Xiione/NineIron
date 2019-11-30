package me.xiione;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public enum ProjectileType {
    SNOWBALL(Material.SNOWBALL, EntityType.SNOWBALL, "snowball"),
    EGG(Material.EGG, EntityType.EGG, "egg"),
    ENDER_PEARL(Material.ENDER_PEARL, EntityType.ENDER_PEARL, "enderpearl"),
    SPLASH_POTION(Material.SPLASH_POTION, EntityType.SPLASH_POTION, "splash-potion"),
    LINGERING_POTION(Material.LINGERING_POTION, EntityType.SPLASH_POTION, "lingering-potion");

    private Material projectileMaterial;
    private EntityType projectile;
    private String key;

    ProjectileType(Material projectileMaterial, EntityType projectile, String name) {
        this.projectileMaterial = projectileMaterial;
        this.projectile = projectile;
        this.key = name;
    }

    /**
     * Checks a material to see if its a projectile source that the nine iron can launch.
     * @param material
     * @return
     */
    public static boolean isProjectileType(Material material) {
        for (ProjectileType type : ProjectileType.values()) {
            if (type.projectileMaterial == material) {
                return true;
            }
        }

        return false;
    }

    public Sound getProjectileSound(FileConfiguration config) { //get impact sound
        String[] values = config.getString(key + "-launch-sound").split("-"); //split config values
        return Sound.valueOf(values[0]);
    }

    public float getProjectileSoundVolume(FileConfiguration config) { //get impact sound volume
        String[] values = config.getString(key + "-launch-sound").split("-");
        return Float.valueOf(values[1]);
    }

    public float getProjectileSoundPitch(FileConfiguration config) { //get impact sound pitch
        String[] values = config.getString(key + "-launch-sound").split("-");
        return Float.valueOf(values[2]);
    }



    /**
     * Returns the projectile material.
     * @return
     */
    public Material getProjectileMaterial() { return  projectileMaterial; }

    /**
     * Returns the projectile entity to use for the nine iron.
     * @return
     */
    public EntityType getProjectile() { return projectile; }

    /**
     * Returns the key name of the projectile.
     * @return
     */
    public String getKey() { return key; }

}
