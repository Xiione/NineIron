package me.xiione;

import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class CooldownCalculator { //inspiration taken from spigot wiki. thanks dudes :)

    private final CooldownManager cooldownManager = new CooldownManager();

    public boolean isAxeAttackReady(Player p) {
        long timeLeft = System.currentTimeMillis() - cooldownManager.getCooldown(p.getUniqueId());
        return (TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= CooldownManager.DEFAULT_COOLDOWN);
    }

    public void setCooldown(Player p){
        cooldownManager.setCooldown(p.getUniqueId(), System.currentTimeMillis());
    }
}
