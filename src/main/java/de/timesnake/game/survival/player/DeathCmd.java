/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.survival.player;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class DeathCmd implements Listener, CommandListener {

    private Code.Permission perm;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.deathMessage(Chat.getSenderPlugin(Plugin.SURVIVAL)
                .append(Server.getUser(e.getEntity()).getChatNameComponent())
                .append(Component.text(" died", ExTextColor.WARNING)));
        Server.getUser(e.getEntity()).asSender(Plugin.SURVIVAL).sendMessageCommandHelp("Teleport to death-point",
                "back");
        ((SurvivalUser) Server.getUser(e.getEntity())).setDeathLocation(e.getEntity().getLocation());
    }

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (sender.hasPermission(this.perm)) {
            if (sender.isPlayer(true)) {
                SurvivalUser user = (SurvivalUser) Server.getUser(sender.getPlayer());
                if (user.getDeathLocation() != null) {
                    sender.getPlayer().teleport(user.getDeathLocation());
                    sender.sendPluginMessage(Component.text("Teleported to death-point", ExTextColor.PERSONAL));
                } else {
                    sender.sendPluginMessage(Component.text("You never died ", ExTextColor.WARNING)
                            .append(Chat.getMessageCode("H", 1906, Plugin.SURVIVAL)));
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return null;
    }

    @Override
    public void loadCodes(de.timesnake.library.extension.util.chat.Plugin plugin) {
        this.perm = plugin.createPermssionCode("sur", "survival.death.back");
    }
}
