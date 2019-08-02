package io.github.animeavi.factionsguard.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.animeavi.factionsguard.FG;
import io.github.animeavi.factionsguard.events.AnimalDamageEvent;
import io.github.animeavi.factionsguard.events.ExplodeEvent;
import io.github.animeavi.factionsguard.events.TeleportEvent;
import net.md_5.bungee.api.ChatColor;

public class ReloadConfig implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        FG.plugin.reloadConfig();
        FG.updateValues();
        TeleportEvent.updateValues();
        ExplodeEvent.updateValues();
        AnimalDamageEvent.updateValues();

        String msg = ChatColor.translateAlternateColorCodes('&',
                FG.plugin.getConfig().getString("config-reloaded-message", "&2FactionsGuard configuration reloaded!"));

        FG.plugin.getLogger().info(ChatColor.stripColor(msg));
        sender.sendMessage(msg);

        return true;
    }

}
