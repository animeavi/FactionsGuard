package io.github.animeavi.factionsguard.events;

import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import io.github.animeavi.factionsguard.FG;
import net.md_5.bungee.api.ChatColor;

public class VillagerDamageEvent implements Listener {
    private static boolean protectVillagers;
    private static boolean showMessage;
    private static String message;
    private static boolean fromPlayer;
    private static boolean fromAnimals;
    private static boolean fromPotions;
    private static boolean fromFireworks;

    public VillagerDamageEvent() {
        updateValues();
    }

    public static void updateValues() {
        protectVillagers = FG.config.getBoolean("villagers-inside-of-factions.protect", true);
        showMessage = FG.config.getBoolean("villagers-inside-of-factions.show-message", true);
        message = FG.config.getString("villagers-inside-of-factions.message", "&6Leave my buddies alone!");
        message = ChatColor.translateAlternateColorCodes('&', message);
        fromPlayer = FG.config.getBoolean("villagers-inside-of-factions.from-player", true);
        fromAnimals = FG.config.getBoolean("villagers-inside-of-factions.from-animals", true);
        fromPotions = FG.config.getBoolean("villagers-inside-of-factions.from-potions", true);
        fromFireworks = FG.config.getBoolean("villagers-inside-of-factions.from-fireworks", true);
    }

    private boolean shouldStop(EntityDamageByEntityEvent event) {
        return !(fromPlayer && fromFireworks && fromAnimals) || !CommonEvent.enabledWorld(event.getEntity().getWorld())
                || !protectVillagers || !(event.getEntity() instanceof Villager);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (shouldStop(event))
            return;

        if (CommonEvent.validDamageCause(event, fromAnimals)) {
            if (CommonEvent.shouldCancelDamage(event, fromPlayer, fromFireworks, fromAnimals, showMessage, message)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!CommonEvent.enabledWorld(event.getEntity().getWorld())) {
            return;
        }

        if (protectVillagers
                && CommonEvent.insideOfPlayerFaction(CommonEvent.getFaction(event.getEntity().getLocation()))) {
            if (!(event.getEntity() instanceof Villager))
                return;

            if (CommonEvent.shouldCancelDamage(event)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!CommonEvent.enabledWorld(event.getEntity().getWorld())) {
            return;
        }

        if (protectVillagers && fromPotions && event.getEntity().getShooter() instanceof Player) {
            if (CommonEvent.shouldCancelSplashDamage(event, CommonEvent.MOB_TYPE.VILLAGER, showMessage, message)) {
                event.setCancelled(true);
            }
        }
    }
}
