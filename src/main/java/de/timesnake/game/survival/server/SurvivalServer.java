/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.survival.messi_chest.MessiChestManager;
import de.timesnake.game.survival.tools.MachineManager;
import org.bukkit.Location;

public class SurvivalServer extends Server {

  public static MachineManager getMachineManager() {
    return server.getMachineManager();
  }

  public static Location getSurvivalSpawn() {
    return server.getSurvivalSpawn();
  }

  public static MessiChestManager getMessiChestManager() {
    return server.getMessiChestManager();
  }

  private static final SurvivalServerManager server = SurvivalServerManager.getInstance();
}
