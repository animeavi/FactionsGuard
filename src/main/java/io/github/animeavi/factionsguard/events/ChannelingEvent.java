package io.github.animeavi.factionsguard.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.LightningStrikeEvent.Cause;

import com.massivecraft.factions.Faction;

import io.github.animeavi.factionsguard.FG;

public class ChannelingEvent implements Listener {
    private static boolean protectChanneling;

    public ChannelingEvent() {
        updateValues();
    }

    public static void updateValues() {
        protectChanneling = FG.config.getBoolean("protect-factions-from-channeling", true);
    }

    @EventHandler
    public void onChannelingStrike(LightningStrikeEvent event) {
        if (!CommonEvent.enabledWorld(event.getWorld()))
            return;

        if (protectChanneling && event.getCause() == Cause.TRIDENT) {
            Faction faction = CommonEvent.getFaction(event.getLightning().getLocation());

            if (CommonEvent.insideOfPlayerFaction(faction)) {
                event.getLightning().remove();
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!CommonEvent.enabledWorld(event.getBlock().getWorld()))
            return;

        if (protectChanneling && event.getCause() == IgniteCause.LIGHTNING) {
            Faction faction = CommonEvent.getFaction(event.getBlock().getLocation());

            if (CommonEvent.insideOfPlayerFaction(faction)) {
                event.setCancelled(true);
            }
        }
    }
}
