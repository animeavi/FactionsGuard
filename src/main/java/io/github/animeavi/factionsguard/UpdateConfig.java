package io.github.animeavi.factionsguard;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;

public class UpdateConfig {
    public static boolean doUpdate(FG plugin, int version) {
        if (version == 0) {
            FileConfiguration config = plugin.getConfig();
            config.set("config-version", 1);
            config.set("villagers-inside-of-factions.protect", true);
            config.set("villagers-inside-of-factions.from-player", true);
            config.set("villagers-inside-of-factions.from-potions", true);
            config.set("villagers-inside-of-factions.from-fireworks", true);
            config.set("villagers-inside-of-factions.show-message", true);
            config.set("villagers-inside-of-factions.message", "&6Leave my buddies alone!");
            saveConfig(plugin, config);
            return true;
        } else if (version == 1) {
            FileConfiguration config = plugin.getConfig();
            config.set("config-version", 2);
            config.set("animals-inside-of-factions.protect-water-animals", false);
            saveConfig(plugin, config);
            return true;
        } else if (version == 2) {
            FileConfiguration config = plugin.getConfig();
            config.set("config-version", 3);
            config.set("protect-factions-from-channeling", true);
            config.set("protect-factions-from-vehicles", true);
            saveConfig(plugin, config);
            return true;
        }

        return false;
    }

    private static void saveConfig(FG plugin, FileConfiguration config) {
        try {
            config.save(plugin.getDataFolder() + File.separator + "config.yml");
        } catch (IOException e) {
        }
    }
}
