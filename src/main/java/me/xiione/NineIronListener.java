package me.xiione;

import io.github.xiione.ConfigUpdater;
import io.github.xiione.UpdateCheck;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Random;

import static org.bukkit.Sound.ENTITY_ITEM_BREAK;
import static org.bukkit.enchantments.Enchantment.DURABILITY;

public class NineIronListener implements Listener {
    //TODO add basic commands and stuff

    private ActualPlugin plugin;
    private final String NINE_IRON_COOLDOWN = "NIcooldown";
    private final String DAMAGE = "NIdamage";
    private final double PLUGIN_VERSION = 1.2;
    public NineIronListener(ActualPlugin passedplugin) {
        this.plugin = passedplugin;
    }

    private int nineiron_level_default, nineiron_launch_delay, nineiron_tool_damage;
    private boolean notify_update, check_enchantment, spawn_particles;
    private String nineiron_enchantment, general_launch_sound;
    private List<String> nineiron_items;

    public void loadConfigs(boolean reload) {
        FileConfiguration config = plugin.getConfig();
        plugin.saveDefaultConfig(); //create the config if it does not exist
        ConfigUpdater.updateConfig(plugin); //add any missing fields
        if (reload) {
            plugin.reloadConfig(); //if being issued via command, reload config values
        }
        nineiron_level_default = config.getInt("nineiron-level-default");
        nineiron_launch_delay = config.getInt("nineiron-launch-delay");
        nineiron_tool_damage = config.getInt("nineiron-tool-damage");

        notify_update = config.getBoolean("notify-update");
        check_enchantment = config.getBoolean("check-enchantment");
        spawn_particles = config.getBoolean("spawn-particles");

        nineiron_enchantment = config.getString("nineiron-enchantment");
        general_launch_sound = config.getString("general-launch-sound");

        nineiron_items = config.getStringList("nineiron_items");
    }

    @EventHandler
    public void axeRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        World w = p.getWorld();
        FileConfiguration config = plugin.getConfig();

        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) { //is the event a mouse2?
            if (isValidToLaunch(p)) { //initial check
                e.setCancelled(true);
                if (MetaDataManager.refreshCoolDown(p, NINE_IRON_COOLDOWN, 1000L, plugin)) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> { //delay, default 0.5 secs
                        ItemStack mainHandItem = p.getInventory().getItemInMainHand();
                        ItemStack offHandItem = p.getInventory().getItemInOffHand();
                        double knockbackVelocity; //"level factor" for damage calculation
                        int kbLevel;
                        if (check_enchantment) {
                            kbLevel = mainHandItem.getEnchantments().get(Enchantment.getByKey(new NamespacedKey(plugin, nineiron_enchantment))) - 1; //base velocity will be 2 at enchantment level 1
                        } else {
                            kbLevel = nineiron_level_default - 1;
                        }

                        knockbackVelocity = 2 + kbLevel * 0.6; //increases velocity by 0.6 per level above base level

                        for (ProjectileType type : ProjectileType.values()) {
                            if (type.getProjectileMaterial() == offHandItem.getType() && isValidToLaunch(p)) { //offhand is a valid item and player still valid to launch
                                if(config.getBoolean("allow-launch-" + type.name())) { //offhand is allowed in config to be launched?
                                    if (type == ProjectileType.SPLASH_POTION || type == ProjectileType.LINGERING_POTION) {
                                        Entity projectile = w.spawnEntity(p.getEyeLocation(), type.getProjectile());
                                        projectile.setVelocity(p.getEyeLocation().getDirection().multiply(knockbackVelocity / 2)); //halve velocity for splash/lingering pots
                                        ((ThrownPotion) projectile).setItem(offHandItem);                                          //no need to include damage metadata for pots
                                        ((Projectile) projectile).setShooter(p); //shoot projectile!
                                    } else {
                                        Entity projectile = w.spawnEntity(p.getEyeLocation(), type.getProjectile());
                                        projectile.setVelocity(p.getEyeLocation().getDirection().multiply(knockbackVelocity));
                                        projectile.setMetadata(DAMAGE, new FixedMetadataValue(plugin, 2 + (kbLevel * 2)));
                                        ((Projectile) projectile).setShooter(p);
                                    }
                                    String[] values = general_launch_sound.split("-");
                                    w.playSound(p.getLocation(), Sound.valueOf(values[0]), Float.valueOf(values[1]), Float.valueOf(values[2])); //Projectile launch sound
                                    w.playSound(p.getLocation(), type.getProjectileSound(config), type.getProjectileSoundVolume(config), type.getProjectileSoundPitch(config));
                                    if(spawn_particles) p.spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 10, 0, 0, 0, 0.05, offHandItem); //creates item_crack effect using offhand item

                                    if (p.getGameMode() != GameMode.CREATIVE) { //if player is not in creative...
                                        Random rand = new Random();

                                        int itemUnbreakingLevel = mainHandItem.getEnchantments().getOrDefault(DURABILITY, 0) + 1; //get unbreaking level
                                        for(int i = 0; i < nineiron_tool_damage; i++) { //roll seperately for each point of damage dealt
                                            if (rand.nextInt(100) <= (100 / itemUnbreakingLevel)) //(100/level + 1) percent chance to deal damage
                                                mainHandItem = addItemDamage(mainHandItem, 1); //deal da damage
                                        }
                                        if (((Damageable) mainHandItem.getItemMeta()).getDamage() >= mainHandItem.getType().getMaxDurability()) {
                                            w.playSound(p.getLocation(), ENTITY_ITEM_BREAK, 1.0F, (Math.min(75, rand.nextInt(51) + 75)) / 100F); //play random pitched breaking sound
                                            p.spawnParticle(Particle.ITEM_CRACK, p.getLocation(), 4, 0, 0, 0, 0.05, mainHandItem); //creates item crack with main hand tool
                                            p.getInventory().setItemInMainHand(null); //clear item
                                        }

                                        offHandItem.setAmount(offHandItem.getAmount() - 1);
                                        p.getInventory().setItemInOffHand(offHandItem); //take one of ammo from player
                                    }
                                    break; //no need to continue looping
                                }
                            }
                        }
                    }, (long) nineiron_launch_delay); //delay of scheduler
                }
            }
        }
    }
    

    @EventHandler
    public void hitByProjectile(EntityDamageByEntityEvent e) {
        for (ProjectileType type : ProjectileType.values()) {

            if (e.getDamager().getType() == type.getProjectile() && e.getDamager().hasMetadata(DAMAGE)) {
                e.setDamage(e.getDamager().getMetadata(DAMAGE).get(0).asInt());
            }
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (notify_update && p.hasPermission("nineiron.notifyupdate")) {
            UpdateCheck
                    .of(plugin)
                    .resourceId(69102)
                    .handleResponse((versionResponse, version) -> {
                        switch (versionResponse) {
                            case FOUND_NEW:
                                p.sendMessage(ChatColor.BLUE + "A new version of NineIron is available!" + ChatColor.GRAY + " (" + ChatColor.GRAY + version + ChatColor.GRAY + ")");
                                p.sendMessage(ChatColor.GRAY + "You can find it here: " + ChatColor.BLUE + "https://www.spigotmc.org/resources/nineiron.69102/");
                                break;
                            case LATEST:
                                break;
                            case UNAVAILABLE:
                                p.sendMessage(ChatColor.RED + "Unable to perform a version check for NineIron.");
                        }
                    }).check();
        }
    }


    private ItemStack addItemDamage(ItemStack item, int damage) { //seperate method for setting item dura/meta cause that stuff confuses me to no end
        Damageable tool = (Damageable) item.getItemMeta();
        tool.setDamage(tool.getDamage() + damage);
        item.setItemMeta((ItemMeta) tool);
        return item;
    }

    private boolean isValidToLaunch(Player p) {
        final ItemStack mainHandItem = p.getInventory().getItemInMainHand();

        boolean isValidMaterial = false;
        boolean isValidEnchanted = false;
        for (String key : nineiron_items) { //is the item in the main hand of the correct material?
            if (mainHandItem.getType().name().equalsIgnoreCase(key)) {
                isValidMaterial = true;
                break;
            }
        }
        if (check_enchantment) { //should an enchantment be checked for?
            if (mainHandItem.getEnchantments().containsKey(Enchantment.getByKey(new NamespacedKey(plugin, nineiron_enchantment))))
                isValidEnchanted = true; //does the item in the main have the correct enchantment?
        } else isValidEnchanted = true;

        return (isValidMaterial && isValidEnchanted);
    }
}




