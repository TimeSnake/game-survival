/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.player;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class RandomTpCmd implements CommandListener {

  private static final int MAX_COORD = 10000;
  private static final int COOLDOWN_MIN = 30;

  private final Map<UUID, LocalDateTime> cooldownUsers = new HashMap<>();
  private final Random random = new Random();

  private final Code permCode = new Code.Builder()
      .setPlugin(Plugin.SURVIVAL)
      .setType(Code.Type.PERMISSION)
      .setPermission("survival.randomtp")
      .build();


  @Override
  public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
    sender.assertElseExit(sender.isPlayer(true));
    sender.hasPermissionElseExit(this.permCode);

    User user = sender.getUser();

    if (this.cooldownUsers.containsKey(user.getUniqueId())) {
      Duration duration = Duration.between(LocalDateTime.now(), this.cooldownUsers.get(user.getUniqueId()));

      if (!duration.isZero()) {
        sender.sendPluginTDMessage("§wYou must wait §v" + Chat.getTimeString(duration));
        return;
      }

      this.cooldownUsers.remove(user.getUniqueId());
    }

    World world = SurvivalServer.getSurvivalSpawn().getWorld();
    int x = this.random.nextInt(MAX_COORD);
    int z = this.random.nextInt(MAX_COORD);
    int y = world.getHighestBlockYAt(x, z);


    this.cooldownUsers.put(user.getUniqueId(), LocalDateTime.now().plusMinutes(30));

    user.teleport(new Location(world, x, y, z));
    sender.sendPluginTDMessage("§sTeleported to §v" + Chat.getLocationString(x, y, z));
  }

  @Override
  public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
    return null;
  }
}
