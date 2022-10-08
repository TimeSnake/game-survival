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

package de.timesnake.game.survival.player;

import de.timesnake.basic.bukkit.util.user.User;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SurvivalUser extends User {

    private Block selectedBlock;
    private Location deathLocation;

    public SurvivalUser(Player player) {
        super(player);
    }

    public Block getSelectedBlock() {
        return selectedBlock;
    }

    public void setSelectedBlock(Block block) {
        this.selectedBlock = block;
    }

    public boolean hasBlockSelected() {
        if (this.selectedBlock != null) {
            return true;
        }
        return false;
    }

    public void resetSelectedBlock() {
        this.selectedBlock = null;
    }

    public Location getDeathLocation() {
        return deathLocation;
    }

    public void setDeathLocation(Location deathLocation) {
        this.deathLocation = deathLocation;
    }

}
