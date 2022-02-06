package de.timesnake.game.survival.machines;

import org.bukkit.Location;
import org.bukkit.event.Listener;

public abstract class Machine implements Listener {

    public enum Type {
        HARVESTER, MINER, STASH;
    }

    protected final Integer id;
    protected final Location location;

    public Machine(Integer id, Location location) {
        this.id = id;
        this.location = location;
    }

    public Integer getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public abstract Machine.Type getType();


}
