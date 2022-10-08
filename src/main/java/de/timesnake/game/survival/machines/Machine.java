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

package de.timesnake.game.survival.machines;


import org.bukkit.block.Block;

public abstract class Machine {

    protected final Integer id;
    protected final Block block;

    public Machine(Integer id, Block block) {
        this.id = id;
        this.block = block;
    }

    public Integer getId() {
        return id;
    }

    public Block getBlock() {
        return block;
    }

    public abstract Machine.Type getType();

    public enum Type {
        HARVESTER,
        MINER,
        STASH;
    }


}
