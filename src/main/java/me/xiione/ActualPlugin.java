package me.xiione;

import io.github.xiione.ConfigUpdater;
import org.bukkit.plugin.java.JavaPlugin;

public class ActualPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final NineIronListener nineIronListener = new NineIronListener(this);
        this.saveDefaultConfig();
        ConfigUpdater.updateConfig(this);

        getServer().getPluginManager().registerEvents(nineIronListener, this);
        nineIronListener.loadConfigs(false);
    }

    @Override
    public void onDisable() {

    }
}
