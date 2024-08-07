/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.tools;

import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;

import java.util.Collection;

public class MachinesFile extends ExFile {

  public static final String MACHINES_PATH = "machines";
  public static final String TYPE = "type";

  private final Logger logger = LogManager.getLogger("survival.machine.file");

  public MachinesFile() {
    super("survival", "machines");
  }

  public Machine getMachine(Integer id) {
    Machine.Type type;
    Block block;

    String typeString = super.getString(MACHINES_PATH + "." + id + "." + TYPE);
    if (typeString == null) {
      this.logger.warn("Can not read type of machine: {}", id);
      return null;
    }
    try {
      type = Machine.Type.valueOf(typeString);
    } catch (IllegalArgumentException e) {
      this.logger.warn("Can not read type of machine: {}", id);
      return null;
    }

    try {
      block = super.getBlock(MACHINES_PATH + "." + id);
    } catch (WorldNotExistException e) {
      this.logger.warn("Can not read location of machine: {}", id);
      return null;
    }

    if (type.equals(Machine.Type.HARVESTER)) {
      if (block.getState() instanceof InventoryHolder) {
        return new Harvester(id, block);
      } else {
        this.logger.warn("Can not load harvester: {}", id);
        return null;
      }
    } else if (type.equals(Machine.Type.STASH)) {
      StashFile stashFile = new StashFile(id);
      Stash stash = new Stash(id, block, stashFile.getStashOwnerId(), stashFile.getStashMembers());
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
    this.logger.info("Saved machine '{}' to file", id);
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
