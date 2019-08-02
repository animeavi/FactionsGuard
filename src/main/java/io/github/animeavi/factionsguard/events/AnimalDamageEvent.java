package io.github.animeavi.factionsguard.events;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

import io.github.animeavi.factionsguard.FG;
import net.md_5.bungee.api.ChatColor;

public class AnimalDamageEvent implements Listener {
    private static boolean protectAnimals;
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
        showMessage = FG.config.getBoolean("animals-inside-of-factions.show-message", true);
        message = FG.config.getString("animals-inside-of-factions.message", "&6Leave my animals alone!");
        message = ChatColor.translateAlternateColorCodes('&', message);
        fromPlayer = FG.config.getBoolean("animals-inside-of-factions.from-player", true);
        fromPotions = FG.config.getBoolean("animals-inside-of-factions.from-potions", true);
        fromFireworks = FG.config.getBoolean("animals-inside-of-factions.from-fireworks", true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(fromPlayer && fromFireworks) || !FG.protectedWorlds.contains(event.getEntity().getWorld().getName())
                || !protectAnimals || !(event.getEntity() instanceof Animals)) {
            return;
        }

        if ((event.getDamager() instanceof Player) || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {

            Entity damager = event.getDamager();
            Player player = null;

            if (damager instanceof Player) {
                player = (Player) damager;
            } else if (damager instanceof Arrow) {
                Arrow arrow = (Arrow) damager;
                if (arrow.getShooter() instanceof Player) {
                    player = (Player) arrow.getShooter();
                }
            }

            Location loc = event.getEntity().getLocation();
            FLocation fLocation = new FLocation(loc);
            Faction faction = Board.getInstance().getFactionAt(fLocation);

            if (faction.isWilderness() || faction.isSafeZone() || faction.isWarZone()) {
                return;
            }

            if (player != null) {
                if (!fromPlayer)
                    return;

                FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
                if (!faction.getFPlayers().contains(fPlayer)) {
                    if (showMessage)
                        player.sendMessage(message);

                    event.setCancelled(true);
                }
            } else {
                // Just cancel fireworks on animals until I find a better way
                // if it even exists)
                if (!fromFireworks)
                    return;

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (fromPotions && event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();

            for (Entity entity : event.getAffectedEntities()) {
                Location loc = entity.getLocation();
                FLocation fLocation = new FLocation(loc);
                FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
                Faction faction = Board.getInstance().getFactionAt(fLocation);

                if (faction.isWilderness() || faction.isSafeZone() || faction.isWarZone()) {
                    return;
                } else if (!faction.getFPlayers().contains(fPlayer)) {
                    if (showMessage)
                        player.sendMessage(message);

                    event.setCancelled(true);
                    break;
                }
            }
        }
    }
}
