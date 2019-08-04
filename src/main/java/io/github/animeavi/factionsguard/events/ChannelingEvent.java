package io.github.animeavi.factionsguard.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.LightningStrikeEvent.Cause;

import com.massivecraft.factions.Faction;

public class ChannelingEvent implements Listener {
    @EventHandler
    public void onChannelingStrike(LightningStrikeEvent event) {
        if (event.getCause() == Cause.TRIDENT) {
            Faction faction = CommonEvent.getFaction(event.getLightning().getLocation());

            if (CommonEvent.insideOfPlayerFaction(faction)) {
                event.setCancelled(true);
            }
        }
    }
}
