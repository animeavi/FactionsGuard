package io.github.animeavi.factionsguard.events;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import io.github.animeavi.factionsguard.FG;
import net.md_5.bungee.api.ChatColor;

public class AnimalDamageEvent implements Listener {
    private static boolean protectAnimals;
    private static boolean protectFishe;
    private static boolean showMessage;
    private static String message;
    private static boolean fromPlayer;
    private static boolean fromPotions;
    private static boolean fromFireworks;

    public AnimalDamageEvent() {
        updateValues();
    }

    public static void updateValues() {
        protectAnimals = FG.config.getBoolean("animals-inside-of-factions.protect", true);
        protectFishe = FG.config.getBoolean("animals-inside-of-factions.protect-water-animals", true);
        showMessage = FG.config.getBoolean("animals-inside-of-factions.show-message", true);
        message = FG.config.getString("animals-inside-of-factions.message", "&6Leave my animals alone!");
        message = ChatColor.translateAlternateColorCodes('&', message);
        fromPlayer = FG.config.getBoolean("animals-inside-of-factions.from-player", true);
        fromPotions = FG.config.getBoolean("animals-inside-of-factions.from-potions", true);
        fromFireworks = FG.config.getBoolean("animals-inside-of-factions.from-fireworks", true);
    }

    private boolean shouldStop(EntityDamageByEntityEvent event) {
        if (protectAnimals) {
            if (!fromPlayer && !fromFireworks) {
                return true;
            } else if (!FG.protectedWorlds.contains(event.getEntity().getWorld().getName())) {
                return true;
            } else if (event.getEntity() instanceof Animals) {
                return false;
            } else if (protectFishe && (event.getEntity() instanceof WaterMob)) {
                return false;
            }
        }

        return true;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (shouldStop(event))
            return;

        if (CommonEvent.validDamageCause(event)) {
            if (CommonEvent.shouldCancelDamage(event, fromPlayer, fromFireworks, showMessage, message)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (fromPotions && event.getEntity().getShooter() instanceof Player) {
            if (CommonEvent.shouldCancelSplashDamage(event, CommonEvent.MOB_TYPE.ANIMALS, showMessage, message)) {
                event.setCancelled(true);
            } else if (protectFishe
                    && CommonEvent.shouldCancelSplashDamage(event, CommonEvent.MOB_TYPE.FISH, showMessage, message)) {
                event.setCancelled(true);
            }
        }
    }
}
