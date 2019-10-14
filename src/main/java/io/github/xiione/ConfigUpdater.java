package io.github.xiione;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigUpdater {

    private final static String HEADER = "Default configuration with extra comments available at https://spigotmc.org/wiki/convenienchant-config";

    public static void updateConfig(JavaPlugin plugin) {
        InputStream customClassStream = plugin.getClass().getResourceAsStream("/config.yml");
        InputStreamReader strR = new InputStreamReader(customClassStream);
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(strR);
        try {
            if(new File(plugin.getDataFolder() + "/config.yml").exists()) {
                boolean changesMade = false;
                YamlConfiguration tmp = new YamlConfiguration();
                tmp.load(plugin.getDataFolder() + "/config.yml");
                for(String str : cfg.getKeys(true)) {
                    if(!tmp.getKeys(true).contains(str)) {
                        tmp.set(str, cfg.get(str));
                        changesMade = true;
                    }
                }
                if(changesMade)
                    tmp.options().header(HEADER);
                    tmp.save(plugin.getDataFolder() + "/config.yml");
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
