package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class Miner extends Machine {

    public static final String NAME = "§rMiner";

    public static final ExItemStack ITEM = new ExItemStack(Material.DROPPER, 1, NAME, List.of("Miner", "§7Generates " +
            "diverse stone blocks"));

    public static final ExItemStack COBBLESTONE = new ExItemStack(Material.COBBLESTONE, 7);
    public static final ExItemStack ANDESITE = new ExItemStack(Material.ANDESITE, 1);
    public static final ExItemStack DIORITE = new ExItemStack(Material.DIORITE, 1);
    public static final ExItemStack GRANIT = new ExItemStack(Material.GRANITE, 1);
    public static final ExItemStack STONE = new ExItemStack(Material.STONE, 1);
    public static final ExItemStack GRAVEL = new ExItemStack(Material.GRAVEL, 1);

    public Miner(Integer id, Location location) {
        super(id, location);
    }

    @Override
    public Machine.Type getType() {
        return Type.MINER;
    }

    public void generate() {
        Inventory inv = ((InventoryHolder) super.getLocation().getBlock().getState()).getInventory();
        inv.addItem(COBBLESTONE);
        inv.addItem(ANDESITE);
        inv.addItem(DIORITE);
        inv.addItem(GRANIT);
        inv.addItem(STONE);
        inv.addItem(GRAVEL);

    }

}
