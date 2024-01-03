/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.player;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.chat.Code;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathCmd implements Listener, CommandListener {

  private final Code perm = Plugin.SURVIVAL.createPermssionCode("survival.death.back");

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent e) {
    e.deathMessage(Chat.getSenderPlugin(Plugin.SURVIVAL)
        .append(Server.getUser(e.getEntity()).getChatNameComponent())
        .append(Component.text(" died", ExTextColor.WARNING)));
    Server.getUser(e.getEntity()).asSender(Plugin.SURVIVAL).sendTDMessageCommandHelp("Teleport to death-point", "back");
    ((SurvivalUser) Server.getUser(e.getEntity())).setDeathLocation(e.getEntity().getLocation());
  }

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
    if (sender.hasPermission(this.perm)) {
      if (sender.isPlayer(true)) {
        SurvivalUser user = (SurvivalUser) Server.getUser(sender.getPlayer());
        if (user.getDeathLocation() != null) {
          sender.getPlayer().teleport(user.getDeathLocation());
          sender.sendPluginMessage(
              Component.text("Teleported to death-point", ExTextColor.PERSONAL));
        } else {
          sender.sendPluginMessage(Component.text("You never died ", ExTextColor.WARNING)
              .append(Chat.getMessageCode("H", 1906, Plugin.SURVIVAL)));
        }
      }
    }
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.perm);
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }
}
