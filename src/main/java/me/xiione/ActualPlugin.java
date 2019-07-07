package me.xiione;

import org.bukkit.plugin.java.JavaPlugin;

public class ActualPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new AxeRightClickListener(this), this);
        getServer().getPluginManager().registerEvents(new ProjectileDamageListener(), this);
    }

    public void onDisable() {

    }
}
