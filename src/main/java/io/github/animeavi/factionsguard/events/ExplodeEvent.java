package io.github.animeavi.factionsguard.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;

import io.github.animeavi.factionsguard.FG;

public class ExplodeEvent implements Listener {
    private static boolean protectTNT;

    public ExplodeEvent() {
        updateValues();
    }

    public static void updateValues() {
        protectTNT = FG.config.getBoolean("disable-tnt-explosion-factions", true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        // Check for primed TNT
        if (entity instanceof TNTPrimed) {
            if (!CommonEvent.enabledWorld(event.getLocation().getWorld()) || !protectTNT) {
                return;
            }

            boolean wDenyBuild = false;
            boolean wzDenyBuild = false;
            boolean szDenyBuild = false;

            if (!FG.legacy) {
                wDenyBuild = FactionsPlugin.getInstance().conf().factions().protection().isWildernessDenyBuild();
                wzDenyBuild = FactionsPlugin.getInstance().conf().factions().protection().isWarZoneDenyBuild();
                szDenyBuild = FactionsPlugin.getInstance().conf().factions().protection().isSafeZoneDenyBuild();
            } else {
                try {
                    Class<?> conf = Class.forName("com.massivecraft.factions.Conf");
                    wDenyBuild = conf.getDeclaredField("wildernessDenyBuild").getBoolean(null);
                    wzDenyBuild = conf.getDeclaredField("warZoneDenyBuild").getBoolean(null);
                    szDenyBuild = conf.getDeclaredField("safeZoneDenyBuild").getBoolean(null);
                } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException
                        | NoSuchFieldException | SecurityException e) {
                }
            }

            boolean canBlowUp = false;
            Faction faction = CommonEvent.getFaction(event.getLocation());
            boolean fWilderness = faction.isWilderness() && !wDenyBuild;
            boolean fWarzone = faction.isWarZone() && !wzDenyBuild;
            boolean fSafezone = faction.isSafeZone() && !szDenyBuild;

            if (fWilderness || fWarzone || fSafezone) {
                canBlowUp = true;
            }

            if (!canBlowUp) {
                event.setCancelled(true);
            }
        }
    }
}
