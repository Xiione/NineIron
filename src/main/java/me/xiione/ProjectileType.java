package me.xiione;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

public enum ProjectileType {
    SNOWBALL(Material.SNOWBALL, EntityType.SNOWBALL, Sound.BLOCK_SNOW_BREAK),
    EGG(Material.EGG, EntityType.EGG, Sound.BLOCK_STONE_HIT),
    ENDER_PEARL(Material.ENDER_PEARL, EntityType.ENDER_PEARL, Sound.ENTITY_ENDER_EYE_DEATH),
    SPLASH_POTION(Material.SPLASH_POTION, EntityType.SPLASH_POTION, Sound.BLOCK_GLASS_HIT),
    LINGERING_POTION(Material.LINGERING_POTION, EntityType.SPLASH_POTION, Sound.BLOCK_GLASS_HIT);

    private Material projectileMaterial;
    private EntityType projectile;
    private Sound impactSound;

    ProjectileType(Material projectileMaterial, EntityType projectile, Sound impactSound) {
        this.projectileMaterial = projectileMaterial;
        this.projectile = projectile;
        this.impactSound = impactSound;
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
     * Returns the impact sound when the projectile hits something.
     * @return
     */
    public Sound getImpactSound() { return  impactSound; }
}
