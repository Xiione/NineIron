package me.xiione;

import org.bukkit.plugin.java.JavaPlugin;

public class ActualPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final NineIronListener nineIronListener = new NineIronListener(this);
        this.saveDefaultConfig();

        this.getCommand("nineiron").setExecutor(nineIronListener);

        getServer().getPluginManager().registerEvents(nineIronListener, this);
        nineIronListener.loadConfigs(false);
    }

    @Override
    public void onDisable() {

    }
}
