package de.timesnake.game.survival.player;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class DeathCmd implements Listener, CommandListener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.setDeathMessage(Chat.getSenderPlugin(Plugin.SURVIVAL) + ChatColor.VALUE + Server.getUser(e.getEntity()).getChatName() + ChatColor.QUICK_INFO + " died");
        Server.getUser(e.getEntity()).asSender(Plugin.SURVIVAL).sendMessageCommandHelp("Teleport to death-point", "back");
        ((SurvivalUser) Server.getUser(e.getEntity())).setDeathLocation(e.getEntity().getLocation());
    }

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (sender.hasPermission("survival.death.back", 1811)) {
            if (sender.isPlayer(true)) {
                SurvivalUser user = (SurvivalUser) Server.getUser(sender.getPlayer());
                if (user.getDeathLocation() != null) {
                    sender.getPlayer().teleport(user.getDeathLocation());
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Teleported to death-point");
                } else {
                    sender.sendPluginMessage(ChatColor.WARNING + "You never died " + Chat.getMessageCode("H", 1906, Plugin.SURVIVAL));
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return null;
    }
}
