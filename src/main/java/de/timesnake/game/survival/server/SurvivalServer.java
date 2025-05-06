/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.survival.messi_chest.MessiChestManager;
import de.timesnake.library.chat.Plugin;
import org.bukkit.Location;

public class SurvivalServer extends Server {

  public static final Plugin PLUGIN = new Plugin("Survival", "GSS");

  public static Location getSurvivalSpawn() {
    return server.getSurvivalSpawn();
  }

  public static MessiChestManager getMessiChestManager() {
    return server.getMessiChestManager();
  }

  private static final SurvivalServerManager server = SurvivalServerManager.getInstance();
}
