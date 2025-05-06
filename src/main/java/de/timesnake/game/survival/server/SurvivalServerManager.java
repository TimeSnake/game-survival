/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.survival.messi_chest.MessiChestManager;
import de.timesnake.game.survival.player.SurvivalUser;
import de.timesnake.game.survival.tools.Crafter;
import de.timesnake.game.survival.tools.SpeedMinecarts;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class SurvivalServerManager extends ServerManager implements Listener {

  public static SurvivalServerManager getInstance() {
    return (SurvivalServerManager) ServerManager.getInstance();
  }

  private SpeedMinecarts speedMinecarts;
  private MessiChestManager messiChestManager;

  private Location survivalSpawn;

  public void onSurvivalEnable() {
    this.messiChestManager = new MessiChestManager();
    Crafter.loadRecipe();
    survivalSpawn = Bukkit.getWorld("world").getSpawnLocation();

    this.speedMinecarts = new SpeedMinecarts();
    this.messiChestManager.load();

    Server.setPvP(false);
  }

  public void onSurvivalDisable() {
    this.messiChestManager.save();
  }

  @Override
  public SurvivalUser loadUser(Player player) {
    return new SurvivalUser(player);
  }

  public Location getSurvivalSpawn() {
    return survivalSpawn;
  }

  public MessiChestManager getMessiChestManager() {
    return messiChestManager;
  }

}
