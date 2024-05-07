/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.survival.player.SurvivalUser;
import de.timesnake.game.survival.tools.MachineManager;
import de.timesnake.game.survival.tools.SpeedMinecarts;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class SurvivalServerManager extends ServerManager implements Listener {

  public static SurvivalServerManager getInstance() {
    return (SurvivalServerManager) ServerManager.getInstance();
  }

  private MachineManager machineManager;
  private SpeedMinecarts speedMinecarts;

  private Location survivalSpawn;

  public void onSurvivalEnable() {
    this.machineManager = new MachineManager();
    survivalSpawn = Bukkit.getWorld("world").getSpawnLocation();

    this.speedMinecarts = new SpeedMinecarts();

    Server.setPvP(false);
  }

  public void onSurvivalDisable() {
    machineManager.saveMachinesToFile();
  }

  @Override
  public SurvivalUser loadUser(Player player) {
    return new SurvivalUser(player);
  }

  public MachineManager getMachineManager() {
    return machineManager;
  }

  public Location getSurvivalSpawn() {
    return survivalSpawn;
  }
}
