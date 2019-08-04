package io.github.animeavi.factionsguard.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.LightningStrikeEvent.Cause;

import com.massivecraft.factions.Faction;

public class ChannelingEvent implements Listener {
    @EventHandler
    public void onChannelingStrike(LightningStrikeEvent event) {
        if (event.getCause() == Cause.TRIDENT) {
            Faction faction = CommonEvent.getFaction(event.getLightning().getLocation());

            if (CommonEvent.insideOfPlayerFaction(faction)) {
                event.getLightning().remove();
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == IgniteCause.LIGHTNING) {
            Faction faction = CommonEvent.getFaction(event.getBlock().getLocation());

            if (CommonEvent.insideOfPlayerFaction(faction)) {
                event.setCancelled(true);
            }
        }
    }
}
