/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.game.survival.messi_chest.MessiChestCmd;
import de.timesnake.game.survival.player.RandomTpCmd;
import de.timesnake.game.survival.player.SurvivalSpawnCmd;
import de.timesnake.game.survival.rewards.RewardHandler;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.game.survival.server.SurvivalServerManager;
import de.timesnake.game.survival.world.SpawnProtection;
import de.timesnake.library.basic.util.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public class GameSurvival extends JavaPlugin {

  public static GameSurvival getPlugin() {
    return plugin;
  }

  private static GameSurvival plugin;

  @Override
  public void onLoad() {
    ServerManager.setInstance(new SurvivalServerManager());
  }

  @Override
  public void onEnable() {
    GameSurvival.plugin = this;

    PluginManager pm = Bukkit.getPluginManager();
    pm.registerEvents(new SpawnProtection(), this);
    pm.registerEvents(SurvivalServerManager.getInstance(), this);
    pm.registerEvents(new RewardHandler(), this);

    Server.getCommandManager().addCommand(this, "survivalspawn", List.of("spawnsurvival", "sspawn"),
        new SurvivalSpawnCmd(), SurvivalServer.PLUGIN);

    Server.getCommandManager().addCommand(this, "messi", new MessiChestCmd(), SurvivalServer.PLUGIN);

    Server.getCommandManager().addCommand(this, "tprandom", List.of("tprand", "randomtp"),
        new RandomTpCmd(), SurvivalServer.PLUGIN);

    ((DbNonTmpGameServer) Database.getServers().getServer(ServerType.GAME, Bukkit.getPort())).setTask("survival");

    SurvivalServerManager.getInstance().onSurvivalEnable();
  }

  @Override
  public void onDisable() {
    SurvivalServerManager.getInstance().onSurvivalDisable();
  }

}
