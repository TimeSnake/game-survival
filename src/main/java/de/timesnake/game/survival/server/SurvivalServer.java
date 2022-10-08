/*
 * game-survival.main
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
