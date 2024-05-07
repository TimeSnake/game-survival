/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.tools;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.survival.main.GameSurvival;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class SpeedMinecarts implements Listener {

  private static final double MAX_SPEED = 0.8;

  public SpeedMinecarts() {
    Server.registerListener(this, GameSurvival.getPlugin());
  }

  @EventHandler
  public void onEntitySpawn(EntitySpawnEvent e) {
    if (!(e.getEntity() instanceof Minecart minecart)) {
      return;
    }

    minecart.setMaxSpeed(MAX_SPEED);
  }
}
