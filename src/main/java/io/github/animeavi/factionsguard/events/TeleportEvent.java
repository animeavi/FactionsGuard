package io.github.animeavi.factionsguard.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.util.SpiralTask;

import io.github.animeavi.factionsguard.FG;
import net.md_5.bungee.api.ChatColor;

public class TeleportEvent implements Listener {
    private static boolean protectChorus;
    private static boolean protectPearl;
    private static String tpMessage;

    public TeleportEvent() {
        updateValues();
    }

    public static void updateValues() {
        protectChorus = FG.config.getBoolean("disallow-chorus-fruit-tp-other-factions", true);
        protectPearl = FG.config.getBoolean("disallow-ender-pearl-tp-other-factions", true);
        tpMessage = FG.config.getString("tp-other-factions-message", "&6I don't think so, Tim.");
        tpMessage = ChatColor.translateAlternateColorCodes('&', tpMessage);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        TeleportCause cause = event.getCause();
        Player player = event.getPlayer();
        if (CommonEvent.isAdminBypassing(player))
            return;

        if (!CommonEvent.enabledWorld(event.getFrom().getWorld())) {
            return;
        } else if (cause.equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) && !protectChorus) {
            return;
        } else if (cause.equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL) && !protectPearl) {
            return;
        }

        if (cause.equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)
                || cause.equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            Location tpLoc = event.getTo();
            Faction faction = CommonEvent.getFaction(tpLoc);

            if (!CommonEvent.insideOfPlayerFaction(faction)) {
                return;
            } else if (!CommonEvent.isPlayerInFaction(player, faction)) {
                player.sendMessage(tpMessage);
                event.setCancelled(true);
                factionTPWilderness(event);
            }
        }
    }

    private void factionTPWilderness(final PlayerTeleportEvent event) {
        // This was copied from the "/f stuck" command
        new BukkitRunnable() {
            @Override
            public void run() {
                final Player player = event.getPlayer();
                final int radius = 10;

                // check for world difference or radius exceeding
                final World world = event.getTo().getWorld();

                final Board board = Board.getInstance();
                // spiral task to find nearest wilderness chunk
                new SpiralTask(new FLocation(player), radius * 2) {

                    @Override
                    public boolean work() {
                        FLocation chunk = currentFLocation();
                        Faction faction = board.getFactionAt(chunk);
                        if (faction.isWilderness()) {
                            int cx = FLocation.chunkToBlock((int) chunk.getX());
                            int cz = FLocation.chunkToBlock((int) chunk.getZ());
                            int y = world.getHighestBlockYAt(cx, cz);
                            Location tp = new Location(world, cx, y, cz);
                            player.teleport(tp);
                            this.stop();
                            return false;
                        }
                        return true;
                    }
                };
            }
        }.runTask(FG.plugin);
    }
}
