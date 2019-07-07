package me.xiione;

import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ProjectileDamageListener implements Listener {

    @EventHandler
    public void hitByProjectile(EntityDamageByEntityEvent e) {
        switch (e.getDamager().getName()) { //what's the entity in question, as a string?
            case "Snowball":
                Snowball ball = (Snowball) e.getDamager();
                if (ball.hasMetadata("damage")) { //does it have the damage metadata?
                    int damage = ball.getMetadata("damage").get(0).asInt(); //get the damage amount attached to the key.
                    e.setDamage(damage);
                }
                break;
            case "Thrown Egg":
                Egg egg = (Egg) e.getDamager();
                if (egg.hasMetadata("damage")) {
                    int damage = egg.getMetadata("damage").get(0).asInt();
                    e.setDamage(damage);
                }
                break;
            case "Thrown Ender Pearl":
                EnderPearl pearl = (EnderPearl) e.getDamager();
                if (pearl.hasMetadata("damage")) {
                    int damage = pearl.getMetadata("damage").get(0).asInt();
                    e.setDamage(damage);
                }
                break;
        }
    }
}