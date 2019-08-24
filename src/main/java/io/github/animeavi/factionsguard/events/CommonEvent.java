package io.github.animeavi.factionsguard.events;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;

import io.github.animeavi.factionsguard.FG;

public class CommonEvent {
    public static enum MOB_TYPE {
        ANIMALS, VILLAGER, FISH
    };

    public static Faction getFaction(Location loc) {
        FLocation fLocation = new FLocation(loc);
        Faction faction = Board.getInstance().getFactionAt(fLocation);

        return faction;
    }

    public static boolean insideOfPlayerFaction(Faction faction) {
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

    public static boolean validDamageCause(EntityDamageByEntityEvent event, boolean fromAnimals) {
        return event.getDamager() instanceof Player
            || (event.getDamager() instanceof Animals && fromAnimals)
            || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE
            || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
            || event.getCause() == EntityDamageEvent.DamageCause.POISON
            || event.getCause() == EntityDamageEvent.DamageCause.WITHER;
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
        } else if (damager instanceof Trident) {
            Trident trident = (Trident) damager;
            if (trident.getShooter() instanceof Player) {
                player = (Player) trident.getShooter();
            }
        }

        return player;
    }

    public static boolean shouldCancelDamage(EntityDamageByEntityEvent event,
            boolean fromPlayer,
            boolean fromAnimals,
            boolean fromFireworks,
            boolean showMessage,
            String message) {

        Entity entity = event.getDamager();
        Faction faction = getFaction(event.getEntity().getLocation());

        if (!insideOfPlayerFaction(faction))
            return false;

        if (getPlayerCausingDamage(event) != null) {
            Player player = getPlayerCausingDamage(event);

            if (isAdminBypassing(player))
                return false;
            if (!fromPlayer)
                return false;

            if (!isPlayerInFaction(player, faction)) {
                if (event.getDamager() instanceof Arrow) {
                    Arrow arrow = (Arrow) event.getDamager();
                    arrow.remove();
                }

                if (showMessage)
                    player.sendMessage(message);
                return true;
            }
        } else if (fromAnimals && (entity instanceof Animals || entity instanceof WaterMob)) { 
            return true;
        } else {
            // Just cancel fireworks until I find a better way (if it even exists)
            if (!fromFireworks)
                return false;
            return true;
        }

        return false;
    }

    public static boolean shouldCancelDamage(EntityDamageEvent event) {
        return event.getCause() == EntityDamageEvent.DamageCause.POISON
                || event.getCause() == EntityDamageEvent.DamageCause.WITHER;
    }

    public static boolean shouldCancelSplashDamage(PotionSplashEvent event, MOB_TYPE type, boolean showMessage,
            String message) {

        Player player = (Player) event.getEntity().getShooter();
        if (isAdminBypassing(player))
            return false;

        for (Entity entity : event.getAffectedEntities()) {
            Faction faction = getFaction(entity.getLocation());
            boolean validType = false;

            if (!insideOfPlayerFaction(faction))
                continue;

            if (type == MOB_TYPE.ANIMALS && (entity instanceof Animals)) {
                validType = true;
            } else if (type == MOB_TYPE.VILLAGER && (entity instanceof Villager)) {
                validType = true;
            } else if (type == MOB_TYPE.FISH && (entity instanceof WaterMob)) {
                validType = true;
            }

            if (validType && !isPlayerInFaction(player, faction)) {
                if (showMessage)
                    player.sendMessage(message);
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static boolean isAdminBypassing(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if (!FG.legacy) {
            return FactionsPlugin.getInstance().conf().factions().protection().getPlayersWhoBypassAllProtection()
                    .contains(fPlayer.getName()) || fPlayer.isAdminBypassing();
        } else {
            try {
                Class<?> conf = Class.forName("com.massivecraft.factions.Conf");
                Set<String> bypassPlayers = (Set<String>) conf.getDeclaredField("playersWhoBypassAllProtection").get(null);
                return bypassPlayers.contains(fPlayer.getName()) || fPlayer.isAdminBypassing();
            } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException
                   | NoSuchFieldException | SecurityException e) {
                return false;
            }
        }
    }

    public static boolean enabledWorld(World world) {
        return FG.protectedWorlds.contains(world.getName());
    }
}
