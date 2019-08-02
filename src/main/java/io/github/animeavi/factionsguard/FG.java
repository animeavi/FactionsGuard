package io.github.animeavi.factionsguard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.animeavi.factionsguard.commands.ReloadConfig;
import io.github.animeavi.factionsguard.events.AnimalDamageEvent;
import io.github.animeavi.factionsguard.events.ExplodeEvent;
import io.github.animeavi.factionsguard.events.TeleportEvent;

public class FG extends JavaPlugin {
    public static FG plugin;
    protected static Server server;
    public static FileConfiguration config;
    public static List<String> protectedWorlds;

    public FG() {
        plugin = this;
        server = plugin.getServer();
        config = this.getConfig();
    }

    public void onEnable() {
        if (resolvePlugin("Factions") != null) {
            plugin.createConfig();
            updateValues();
            server.getPluginManager().registerEvents(new ExplodeEvent(), this);
            server.getPluginManager().registerEvents(new TeleportEvent(), this);
            server.getPluginManager().registerEvents(new AnimalDamageEvent(), this);
            getCommand("fgreload").setExecutor(new ReloadConfig());
        }
    }

    public static void updateValues() {
        config = plugin.getConfig();
        protectedWorlds = getProtectedWorlds();
    }

    @SuppressWarnings("unused")
    private void createConfig() {
        try {
            File file;
            if (!this.getDataFolder().exists()) {
                this.getDataFolder().mkdirs();
            }
            if (!(file = new File(this.getDataFolder(), "config.yml")).exists()) {
                this.getLogger().info("Configuration not found, creating!");
                this.saveDefaultConfig();
            } else {
                this.getLogger().info("Configuration found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> getProtectedWorlds() {
        protectedWorlds = config.getStringList("protected-worlds");
        
        if (protectedWorlds.isEmpty()) {
            protectedWorlds = new ArrayList<String>();
            protectedWorlds.add("world");
        }

        return protectedWorlds;
    }

    private Plugin resolvePlugin(String name) {
        Plugin temp = getServer().getPluginManager().getPlugin(name);

        if (temp == null) {
            return null;
        }

        return temp;
    }
}
