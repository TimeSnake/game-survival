package de.timesnake.game.survival.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.game.survival.machines.MachineManager;
import de.timesnake.game.survival.privates.PrivateBlockManger;
import org.bukkit.Location;

public class SurvivalServer extends Server {

    private static final SurvivalServerManager server = SurvivalServerManager.getInstance();

    public static PrivateBlockManger getPrivateBlockManger() {
        return server.getPrivateBlockManger();
    }

    public static MachineManager getMachineManager() {
        return server.getMachineManager();
    }

    public static Location getSurvivalSpawn() {
        return server.getSurvivalSpawn();
    }
}
