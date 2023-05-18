/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.survival.machines.MachineManager;
import org.bukkit.Location;

public class SurvivalServer extends Server {

  public static MachineManager getMachineManager() {
    return server.getMachineManager();
  }

  public static Location getSurvivalSpawn() {
    return server.getSurvivalSpawn();
  }

  private static final SurvivalServerManager server = SurvivalServerManager.getInstance();
}
