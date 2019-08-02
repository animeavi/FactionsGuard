package io.github.animeavi.factionsguard.events;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

public class CommonEvent {
    public static enum MOB_TYPE {
        ANIMALS, VILLAGER
    };

    public static Faction getFaction(Entity entity) {
        Location loc = entity.getLocation();
        FLocation fLocation = new FLocation(loc);
        Faction faction = Board.getInstance().getFactionAt(fLocation);

        return faction;
    }

    public static boolean mobInsidePlayerFaction(Faction faction) {
        if (faction.isWilderness() || faction.isSafeZone() || faction.isWarZone()) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isPlayerInFaction(Player player, Faction faction) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        return faction.getFPlayers().contains(fPlayer);
    }

    public static boolean validDamageCause(EntityDamageByEntityEvent event) {
        return (event.getDamager() instanceof Player) ||
                event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE ||
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
    }

    public static Player getPlayerCausingDamage(EntityDamageByEntityEvent event) {
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

        return player;
    }

    public static boolean shouldCancelDamage(EntityDamageByEntityEvent event,
                                             boolean fromPlayer,
                                             boolean fromFireworks,
                                             boolean showMessage,
                                             String message) {

        Player player = getPlayerCausingDamage(event);
        Faction faction = getFaction(event.getEntity());

        if (!mobInsidePlayerFaction(faction)) return false;

        if (player != null) {
            if (!fromPlayer) return false;

            if (!isPlayerInFaction(player, faction)) {
                if (event.getDamager() instanceof Arrow) {
                    Arrow arrow = (Arrow) event.getDamager();
                    arrow.remove();
                }

                if (showMessage) player.sendMessage(message);
                return true;
            }
        } else {
            // Just cancel fireworks until I find a better way (if it even exists)
            if (!fromFireworks) return false;
            return true;
        }

        return false;
    }

    public static boolean shouldCancelSplashDamage(PotionSplashEvent event,
                                                   MOB_TYPE type,
                                                   boolean showMessage,
                                                   String message) {

        Player player = (Player) event.getEntity().getShooter();

        for (Entity entity : event.getAffectedEntities()) {
            Faction faction = getFaction(entity);
            boolean validType = false;

            if (!mobInsidePlayerFaction(faction)) continue;

            if (type == MOB_TYPE.ANIMALS && (entity instanceof Animals)) {
                validType = true;
            } else if (type == MOB_TYPE.VILLAGER && (entity instanceof Villager)) {
                validType = true;
            }

            if (validType && !isPlayerInFaction(player, faction)) {
                if (showMessage) player.sendMessage(message);
                return true;
            }
        }

        return false;
    }
}
