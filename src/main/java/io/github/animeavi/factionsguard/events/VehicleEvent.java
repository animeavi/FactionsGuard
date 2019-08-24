package io.github.animeavi.factionsguard.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.perms.PermissibleAction;

import io.github.animeavi.factionsguard.FG;

public class VehicleEvent implements Listener {
    private static boolean protectVehicles;

    public VehicleEvent() {
        updateValues();
    }

    public static void updateValues() {
        protectVehicles = FG.config.getBoolean("protect-factions-from-vehicles", true);
    }

    @EventHandler
    public void onPlaceVehicle(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!CommonEvent.enabledWorld(event.getPlayer().getWorld()) || CommonEvent.isAdminBypassing(player)) {
            return;
        }

        if (protectVehicles && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            EquipmentSlot slot = event.getHand();
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            boolean shouldRemoveVehicle = false;

            if (slot.equals(EquipmentSlot.HAND) && !isAllowedVehicle(mainHandItem)) {
                shouldRemoveVehicle = true;
            } else if (slot.equals(EquipmentSlot.OFF_HAND) && !isAllowedVehicle(offHandItem)) {
                shouldRemoveVehicle = true;
            }

            if (shouldRemoveVehicle) {
                Faction faction = CommonEvent.getFaction(event.getClickedBlock().getLocation());
                FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

                if (CommonEvent.insideOfPlayerFaction(faction)) {
                    if (!CommonEvent.isPlayerInFaction(player, faction)) {
                        if (!FG.legacy && !faction.hasAccess(fPlayer, PermissibleAction.CONTAINER)) {
                            event.setCancelled(true);
                        } else if (FG.legacy && !CommonEvent.isPlayerInFaction(player, faction)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDestroyVehicle(VehicleDestroyEvent event) {
        if (!CommonEvent.enabledWorld(event.getVehicle().getWorld()))
            return;

        if (protectVehicles && (event.getAttacker() instanceof Player)) {
            Faction faction = CommonEvent.getFaction(event.getVehicle().getLocation());
            Player player = (Player) event.getAttacker();
            if (CommonEvent.isAdminBypassing(player))
                return;

            FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

            if (CommonEvent.insideOfPlayerFaction(faction)) {
                if (!CommonEvent.isPlayerInFaction(player, faction)) {
                    if (!FG.legacy && !faction.hasAccess(fPlayer, PermissibleAction.CONTAINER)) {
                        event.setCancelled(true);
                    } else if (FG.legacy && !CommonEvent.isPlayerInFaction(player, faction)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private boolean isAllowedVehicle(ItemStack heldItem) {
        String itemName = heldItem.getType().toString();
        if (itemName.endsWith("_BOAT")) {
            return false;
        } else if (itemName.contains("MINECART")) {
            return false;
        }
        return true;
    }
}
