package me.xiione;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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

public class AxeRightClickListener implements Listener {

    private ActualPlugin plugin;

    public AxeRightClickListener(ActualPlugin passedplugin) {
        this.plugin = passedplugin;
    }

    CooldownCalculator cooldownCalculator = new CooldownCalculator();

    @EventHandler
    public void axeRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        World w = p.getWorld();
        Vector v = p.getLocation().getDirection().normalize();
        ItemStack mainHandItem = p.getInventory().getItemInMainHand();
        ItemStack offHandItem = p.getInventory().getItemInOffHand();
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && //is the event a mouse2?
                mainHandItem.getType().name().endsWith("_AXE") && mainHandItem.hasItemMeta() && //is the item in the main hand an axe, has NBT data, and has the Knockback enchantment?
                mainHandItem.getItemMeta().hasEnchant(KNOCKBACK) &&
                (offHandItem.getType() == Material.SNOWBALL || offHandItem.getType() == Material.EGG ||
                        offHandItem.getType() == Material.ENDER_PEARL)) {
            e.setCancelled(true);
            if (cooldownCalculator.isAxeAttackReady(p)) {
                cooldownCalculator.setCooldown(p);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { //delay, 0.5 secs
                    public void run() {
                        ItemStack mainHandItem = p.getInventory().getItemInMainHand();
                        ItemStack offHandItem = p.getInventory().getItemInOffHand();
                        mainHandItem = p.getInventory().getItemInMainHand();
                        offHandItem = p.getInventory().getItemInOffHand();
                        if ((mainHandItem.getType().name().endsWith("_AXE") && mainHandItem.hasItemMeta() && //is the player's held item STILL meeting the conditions above?
                                mainHandItem.getItemMeta().hasEnchant(KNOCKBACK))) {
                            Vector vector = p.getEyeLocation().getDirection();
                            double knockbackLevelFactor; //"level factor" for damage calculation
                            double knockbackLevel = mainHandItem.getItemMeta().getEnchantLevel(KNOCKBACK); //level of knockback
                            switch (mainHandItem.getItemMeta().getEnchantLevel(KNOCKBACK)) {
                                case 1:
                                    knockbackLevelFactor = 2.0;
                                    break;
                                case 2:
                                    knockbackLevelFactor = 2.60;
                                    break;
                                default:
                                    knockbackLevelFactor = 2.0; //knockback levels above 2 default to 2.0.
                                    break;
                            }
                            switch (offHandItem.getType()) { //get the item in offhand
                                case SNOWBALL:
                                    Snowball ball = p.launchProjectile(Snowball.class, vector);
                                    ball.setVelocity(p.getEyeLocation().getDirection().multiply(knockbackLevelFactor));
                                    ball.setMetadata("damage", new FixedMetadataValue(plugin, knockbackLevel * 2)); //add damage to specific projectile
                                    w.playSound(p.getLocation(), BLOCK_SNOW_BREAK, 1.0F, 1.0F); //impact/hit sound of projectile/ammo
                                    break;
                                case EGG:
                                    Egg egg = p.launchProjectile(Egg.class, vector);
                                    egg.setVelocity(p.getEyeLocation().getDirection().multiply(knockbackLevelFactor));
                                    egg.setMetadata("damage", new FixedMetadataValue(plugin, knockbackLevel * 2));
                                    w.playSound(p.getLocation(), BLOCK_STONE_HIT, 1.0F, 1.5F);
                                    break;
                                case ENDER_PEARL:
                                    EnderPearl pearl = p.launchProjectile(EnderPearl.class, vector);
                                    pearl.setVelocity(p.getEyeLocation().getDirection().multiply(knockbackLevelFactor));
                                    pearl.setMetadata("damage", new FixedMetadataValue(plugin, knockbackLevel * 2));
                                    w.playSound(p.getLocation(), ENTITY_ENDER_EYE_DEATH, 1.0F, 1.0F);
                                    break;
                            }
                            p.spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 10, 0, 0, 0, 0.1, offHandItem); //creates item_crack effect using offhand item
                            if (p.getGameMode() != GameMode.CREATIVE) { //if player is not in creative...
                                Random rand = new Random();
                                if (mainHandItem.getItemMeta().hasEnchant(DURABILITY) && (rand.nextInt(100) + 1) < (100 / mainHandItem.getItemMeta().getEnchantLevel(DURABILITY) + 1)) { //factoring in unbreaking (if present) into durability
                                    if (((Damageable) (mainHandItem.getItemMeta())).getDamage() >= getToolMaxDura(mainHandItem) - 2) { //will the axe break after this use?
                                        w.playSound(p.getLocation(), ENTITY_ITEM_BREAK, 1.0F, ((ThreadLocalRandom.current().nextInt(75, 126)) / 100F)); //play random pitched breaking sound
                                        p.getInventory().setItemInMainHand(null); //clear item
                                    } else {
                                        p.getInventory().setItemInMainHand(setItemDamage(mainHandItem, (((Damageable) (mainHandItem.getItemMeta())).getDamage() + 2))); //otherwise, damage axe by 2
                                    }
                                } else { //if unbreaking is not present
                                    if (((Damageable) (mainHandItem.getItemMeta())).getDamage() >= getToolMaxDura(mainHandItem) - 2) {
                                        w.playSound(p.getLocation(), ENTITY_ITEM_BREAK, 1.0F, ((ThreadLocalRandom.current().nextInt(75, 126)) / 100F));
                                        p.getInventory().setItemInMainHand(null);
                                    } else {
                                        p.getInventory().setItemInMainHand(setItemDamage(mainHandItem, ((Damageable) (mainHandItem.getItemMeta())).getDamage() + 2));
                                    }
                                }
                                p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1); //take one of ammo from player
                            }
                            w.playSound(p.getLocation(), ENTITY_PLAYER_ATTACK_STRONG, 1.0F, 1.0F); //actual "impact" noise

                        }
                    }
                }, 10L); //delay of scheduler
            }
        }
    }


    public static ItemStack setItemDamage(ItemStack item, int damage) { //seperate method for setting item dura/meta cause that stuff confuses me to no end
        Damageable im = (Damageable) item.getItemMeta();
        im.setDamage(damage);
        item.setItemMeta((ItemMeta) im);
        return item;
    }

    public static int getToolMaxDura(ItemStack i) { //the only way i could think of to get constant max duras for each tier of axe
        String iname = i.getType().name();
        if (iname.contains("WOODEN")) {
            return 59;
        } else if (iname.contains("GOLDEN")) {
            return 32;
        } else if (iname.contains("STONE")) {
            return 131;
        } else if (iname.contains("IRON")) {
            return 250;
        } else {
            return 1561;
        }
    }
}

