/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.object.Type;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.game.survival.machines.StashCmd;
import de.timesnake.game.survival.player.DeathCmd;
import de.timesnake.game.survival.player.SurvivalSpawnCmd;
import de.timesnake.game.survival.rewards.RewardHandler;
import de.timesnake.game.survival.server.SurvivalServerManager;
import de.timesnake.game.survival.world.SpawnProtection;
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
        pm.registerEvents(new DeathCmd(), this);
        pm.registerEvents(SurvivalServerManager.getInstance(), this);
        pm.registerEvents(new RewardHandler(), this);

        //Server.getCommandManager().addCommand(this, "private", List.of("pv"), new PrivateCmd(),
        //		Plugin.PRIVATE_BLOCKS);
        Server.getCommandManager().addCommand(this, "back", new DeathCmd(), Plugin.SURVIVAL);
        Server.getCommandManager().addCommand(this, "survivalspawn", List.of("spawnsurvival", "sspawn"),
                new SurvivalSpawnCmd(), Plugin.SURVIVAL);

        Server.getCommandManager().addCommand(this, "stash", new StashCmd(), Plugin.SURVIVAL);

        Database.getServers().getServer(Type.Server.GAME, Bukkit.getPort()).setTask("survival");

        SurvivalServerManager.getInstance().onSurvivalEnable();

        Server.printText(Plugin.SURVIVAL, "Loaded successfully");
    }

    @Override
    public void onDisable() {
        SurvivalServerManager.getInstance().onSurvivalDisable();
    }

}
