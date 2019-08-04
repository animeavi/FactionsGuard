package io.github.animeavi.factionsguard.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.Faction;
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
            if (!FG.protectedWorlds.contains(event.getLocation().getWorld().getName()) || !protectTNT) {
                return;
            }

            boolean canBlowUp = false;
            Faction faction = CommonEvent.getFaction(event.getLocation());
            boolean fWilderness = faction.isWilderness() && !Conf.wildernessDenyBuild;
            boolean fWarzone = faction.isWarZone() && !Conf.warZoneDenyBuild;
            boolean fSafezone = faction.isSafeZone() && !Conf.safeZoneDenyBuild;

            if (fWilderness || fWarzone || fSafezone) {
                canBlowUp = true;
            }

            if (!canBlowUp) {
                event.setCancelled(true);
            }
        }
    }
}
