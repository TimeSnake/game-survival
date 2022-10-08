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

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.game.survival.chat.Plugin;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;

import java.util.Collection;

public class MachinesFile extends ExFile {

    public static final String MACHINES_PATH = "machines";
    public static final String TYPE = "type";

    public MachinesFile() {
        super("survival", "machines");
    }

    public Machine getMachine(Integer id) {
        Machine.Type type;
        Block block;

        String typeString = super.getString(MACHINES_PATH + "." + id + "." + TYPE);
        if (typeString == null) {
            Server.printWarning(Plugin.MACHINES, "Can not read type of machine: " + id);
            return null;
        }
        try {
            type = Machine.Type.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            Server.printWarning(Plugin.MACHINES, "Can not read type of machine: " + id);
            return null;
        }

        try {
            block = super.getBlock(MACHINES_PATH + "." + id);
        } catch (WorldNotExistException e) {
            Server.printWarning(Plugin.MACHINES, "Can not read location of machine: " + id);
            return null;
        }


        if (type.equals(Machine.Type.HARVESTER)) {
            if (block.getState() instanceof InventoryHolder) {
                return new Harvester(id, block);
            } else {
                Server.printWarning(Plugin.MACHINES, "Can not load harvester: " + id);
                return null;
            }
        } else if (type.equals(Machine.Type.STASH)) {
            StashFile stashFile = new StashFile(id);
            Stash stash = new Stash(id, block, stashFile.getStashOwnerId());
            stashFile.getStashItems().forEach(stash::addItem);
            stash.updateInventories();
            return stash;
        }
        return null;
    }

    public Collection<Integer> getMachineIds() {
        return super.getPathIntegerList(MACHINES_PATH);

    }

    public void addMachine(Machine machine) {
        Integer id = machine.getId();
        super.set(MACHINES_PATH + "." + id, machine.getBlock()).save();
        super.set(MACHINES_PATH + "." + id + "." + "type", machine.getType().name()).save();

        if (machine instanceof Stash stash) {
            StashFile stashFile = new StashFile(id);
            stashFile.delete();
            stashFile.create();
            stashFile.saveStash(stash.getBlock(), stash.getOwner(), stash.getMembers(), stash.getItems());
        }
        Server.printText(Plugin.MACHINES, "Saved machine " + id + " to file");
        super.save();
    }

    public void removeMachine(Machine machine) {
        Integer id = machine.getId();
        super.remove(MACHINES_PATH + "." + id);

        if (machine instanceof Stash) {
            new StashFile(id).delete();
        }

        super.save();
    }

    public void resetMachines() {
        super.remove(MACHINES_PATH);
    }
}
