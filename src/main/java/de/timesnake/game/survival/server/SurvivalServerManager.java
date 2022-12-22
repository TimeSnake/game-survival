/*
 * workspace.game-survival.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.survival.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.survival.machines.MachineManager;
import de.timesnake.game.survival.player.SurvivalUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class SurvivalServerManager extends ServerManager implements Listener {

    public static SurvivalServerManager getInstance() {
        return (SurvivalServerManager) ServerManager.getInstance();
    }

    private MachineManager machineManager;

    private Location survivalSpawn;

    public void onSurvivalEnable() {
        this.machineManager = new MachineManager();
        survivalSpawn = Bukkit.getWorld("world").getSpawnLocation();

        Server.setPvP(false);

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

    public MachineManager getMachineManager() {
        return machineManager;
    }

    public Location getSurvivalSpawn() {
        return survivalSpawn;
    }
}
