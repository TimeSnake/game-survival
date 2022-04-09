package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.game.survival.chat.Plugin;
import org.bukkit.Location;
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
        Location loc;

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
            loc = super.getLocation(MACHINES_PATH + "." + id);
        } catch (WorldNotExistException e) {
            Server.printWarning(Plugin.MACHINES, "Can not read location of machine: " + id);
            return null;
        }


        if (type.equals(Machine.Type.HARVESTER)) {
            if (loc.getBlock().getState() instanceof InventoryHolder) {
                return new Harvester(id, loc);
            } else {
                Server.printWarning(Plugin.MACHINES, "Can not load harvester: " + id);
                return null;
            }
        } else if (type.equals(Machine.Type.MINER)) {
            return new Miner(id, loc);
        }
        return null;
    }

    public Collection<Integer> getMachineIds() {
        return super.getPathIntegerList(MACHINES_PATH);

    }

    public void addMachine(Machine machine) {
        Integer id = machine.getId();
        super.set(MACHINES_PATH + "." + id, machine.getLocation().getBlock()).save();
        super.set(MACHINES_PATH + "." + id + "." + "type", machine.getType().name()).save();
        Server.printText(Plugin.MACHINES, "Saved machine " + id + " to file");
    }

    public void removeMachine(Machine machine) {
        Integer id = machine.getId();
        super.remove(MACHINES_PATH + "." + id);
    }

    public void resetMachines() {
        super.remove(MACHINES_PATH);
    }
}
