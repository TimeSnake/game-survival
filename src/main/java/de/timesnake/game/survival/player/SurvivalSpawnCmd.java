/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.player;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import de.timesnake.library.extension.util.chat.Code;
import net.kyori.adventure.text.Component;

public class SurvivalSpawnCmd implements CommandListener {

  private final Code perm = Plugin.SURVIVAL.createPermssionCode("game.survival.survivalspawn");

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
    if (!sender.isPlayer(true)) {
      return;
    }

    if (!sender.hasPermission(this.perm)) {
      return;
    }

    sender.getUser().teleport(SurvivalServer.getSurvivalSpawn());
    sender.sendPluginMessage(
        Component.text("Teleported to survival-spawn", ExTextColor.PERSONAL));
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
