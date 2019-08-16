package me.xiione;

import org.bukkit.plugin.java.JavaPlugin;

public class ActualPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new NineIronListener(this), this);
    }

    @Override
    public void onDisable() {

    }
}
