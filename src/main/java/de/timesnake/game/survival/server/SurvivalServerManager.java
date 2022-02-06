package de.timesnake.game.survival.server;

import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.survival.machines.MachineManager;
import de.timesnake.game.survival.player.SurvivalUser;
import de.timesnake.game.survival.privates.PrivateBlockManger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class SurvivalServerManager extends ServerManager implements Listener {

    public static SurvivalServerManager getInstance() {
        return (SurvivalServerManager) ServerManager.getInstance();
    }

    private PrivateBlockManger privateBlockManger;
    private MachineManager machineManager;

    private Location survivalSpawn;

    public void onSurvivalEnable() {
        this.machineManager = new MachineManager();
        survivalSpawn = Bukkit.getWorld("world").getSpawnLocation();

        //Server.registerListener(privateBlockManger, GameSurvival.getPlugin());
    }

    public void onSurvivalDisable() {
        //privateBlockManger.savePrivateBlocksToFile();
        machineManager.saveMachinesToFile();
    }

    @Override
    public SurvivalUser loadUser(Player player) {
        return new SurvivalUser(player);
    }

    public PrivateBlockManger getPrivateBlockManger() {
        return privateBlockManger;
    }

    public MachineManager getMachineManager() {
        return machineManager;
    }

    public Location getSurvivalSpawn() {
        return survivalSpawn;
    }
}
