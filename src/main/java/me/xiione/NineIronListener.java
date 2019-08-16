package me.xiione;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Sound.*;
import static org.bukkit.enchantments.Enchantment.*;

public class NineIronListener implements Listener {

    private ActualPlugin plugin;
    private final String NINE_IRON_COOLDOWN = "NIcooldown";
    private final String DAMAGE = "NIdamage";

    public NineIronListener(ActualPlugin passedplugin) {
        this.plugin = passedplugin;
    }

    @EventHandler
    public void axeRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        World w = p.getWorld();

        final ItemStack mainHandItem = p.getInventory().getItemInMainHand();
        final ItemStack offHandItem = p.getInventory().getItemInOffHand();

        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && //is the event a mouse2?
                mainHandItem.getType().name().endsWith("_AXE") && mainHandItem.getEnchantments().containsKey(KNOCKBACK) //is the item in the main hand an axe, and has the Knockback enchantment?
                 && ProjectileType.isProjectileType(offHandItem.getType())) {

            e.setCancelled(true);

            if (MetaDataManager.refreshCoolDown(p, NINE_IRON_COOLDOWN, 1000L, plugin)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> { //delay, 0.5 secs
                    double knockbackVelocity; //"level factor" for damage calculation
                    int kbLevel = Math.min(mainHandItem.getEnchantments().get(KNOCKBACK) - 1, 1); //Caps knockback to level 2 and gives a range starting at zero so we can use a base and multiplier.

                    knockbackVelocity = 2 + kbLevel * 0.6; //Increases velocity by 0.6 per level above knockback 1 (capped at 2.6)

                    for (ProjectileType type : ProjectileType.values()) {
                        if (type.getProjectileMaterial() == offHandItem.getType()) {
                            if(type == ProjectileType.SPLASH_POTION || type == ProjectileType.LINGERING_POTION) {
                                Entity projectile = w.spawnEntity(p.getEyeLocation(), type.getProjectile());
                                projectile.setVelocity(p.getEyeLocation().getDirection().multiply(knockbackVelocity / 2)); //halve velocity for splash/lingering pots
                                ((ThrownPotion) projectile).setItem(offHandItem);                                          //no need to include damage metadata for pots
                                ((Projectile) projectile).setShooter(p);
                            } else {
                                Entity projectile = w.spawnEntity(p.getEyeLocation(), type.getProjectile());
                                projectile.setVelocity(p.getEyeLocation().getDirection().multiply(knockbackVelocity));
                                projectile.setMetadata(DAMAGE, new FixedMetadataValue(plugin, 2 + (kbLevel * 2)));
                                ((Projectile) projectile).setShooter(p);
                            }
                        }
                    }


                    p.spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 10, 0, 0, 0, 0.1, offHandItem); //creates item_crack effect using offhand item

                    if (p.getGameMode() != GameMode.CREATIVE) { //if player is not in creative...
                        Random rand = new Random();
                        boolean damageItem = rand.nextInt(100) <= (100 / (mainHandItem.getEnchantments().getOrDefault(DURABILITY, 0) + 1));
                        if (damageItem) {
                            setItemDamage(mainHandItem, 1);
                        }

                        if (((Damageable) mainHandItem.getItemMeta()).getDamage() >= mainHandItem.getType().getMaxDurability()) {
                            w.playSound(p.getLocation(), ENTITY_ITEM_BREAK, 1.0F, (Math.min(75, rand.nextInt(51) + 75)) / 100F); //play random pitched breaking sound
                            p.getInventory().setItemInMainHand(null); //clear item
                        }

                        offHandItem.setAmount(offHandItem.getAmount() - 1);
                        p.getInventory().setItemInOffHand(offHandItem); //take one of ammo from player
                    }

                    w.playSound(p.getLocation(), ENTITY_PLAYER_ATTACK_STRONG, 1.0F, 1.0F); //Projectile launch sound

                }, 10L); //delay of scheduler
            }
        }
    }

    @EventHandler
    public void hitByProjectile(EntityDamageByEntityEvent e) {
        for (ProjectileType type : ProjectileType.values()) {

            if (e.getDamager().getType() == type.getProjectile() && e.getDamager().hasMetadata(DAMAGE)) {
                e.setDamage(e.getDamager().getMetadata(DAMAGE).get(0).asInt());
                e.getEntity().getWorld().playSound(e.getDamager().getLocation(), type.getImpactSound(), 1.0F, 1.0F); //Actual hit sound.
            }
        }
    }

    private ItemStack setItemDamage(ItemStack item, int damage) { //seperate method for setting item dura/meta cause that stuff confuses me to no end
        Damageable tool = (Damageable) item.getItemMeta();
        tool.setDamage(tool.getDamage() + damage);
        item.setItemMeta((ItemMeta) tool);
        return item;
    }
}

