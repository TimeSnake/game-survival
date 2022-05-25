package de.timesnake.game.survival.privates;

import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;
import java.util.UUID;


public class PrivateInventoryBlock extends PrivateBlock {

    private Inventory inventory;

    public PrivateInventoryBlock(Integer id, Block block, UUID owner, String password, boolean isPublic,
                                 List<UUID> members) {
        super(id, block, owner, password, isPublic, members);
        if (block.getState() instanceof InventoryHolder) {
            this.inventory = ((InventoryHolder) block.getState()).getInventory();
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

}
