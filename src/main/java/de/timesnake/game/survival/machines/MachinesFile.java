/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.library.basic.util.Loggers;
import java.util.Collection;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;

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
            Loggers.SURVIVAL.warning("Can not read type of machine: " + id);
            return null;
        }
        try {
            type = Machine.Type.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            Loggers.SURVIVAL.warning("Can not read type of machine: " + id);
            return null;
        }

        try {
            block = super.getBlock(MACHINES_PATH + "." + id);
        } catch (WorldNotExistException e) {
            Loggers.SURVIVAL.warning("Can not read location of machine: " + id);
            return null;
        }

        if (type.equals(Machine.Type.HARVESTER)) {
            if (block.getState() instanceof InventoryHolder) {
                return new Harvester(id, block);
            } else {
                Loggers.SURVIVAL.warning("Can not load harvester: " + id);
                return null;
            }
        } else if (type.equals(Machine.Type.STASH)) {
            StashFile stashFile = new StashFile(id);
            Stash stash = new Stash(id, block, stashFile.getStashOwnerId(),
                    stashFile.getStashMembers());
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
            stashFile.saveStash(stash.getBlock(), stash.getOwner(), stash.getMembers(),
                    stash.getItems());
        }
        Loggers.SURVIVAL.info("Saved machine " + id + " to file");
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
