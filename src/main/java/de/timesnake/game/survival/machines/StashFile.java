package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StashFile extends ExFile {

    private static final String ID = "id";

    private static final String BLOCK = "block";
    private static final String OWNER = "owner";
    private static final String MEMBERS = "members";
    private static final String ITEMS = "items";

    public StashFile(String name) {
        super("survival", "stash_" + name);
    }

    public void addStash(Integer id, Block block, UUID owner, List<UUID> members, List<StashItem> items) {
        super.setBlock(id + "." + BLOCK, block).save();
        super.set(id + "." + OWNER, owner.toString()).save();

        super.setUuidList(id + "." + MEMBERS, members);

        int i = 0;
        for (StashItem item : items) {
            super.set(id + "." + ITEMS + "." + i, item);
        }
        super.save();
    }

    public void removeStash(Integer id) {
        super.remove(String.valueOf(id));
    }

    public Block getStashBlock(Integer id) throws WorldNotExistException {
        return super.getBlock(id + "." + BLOCK);
    }

    public UUID getStashOwnerId(Integer id) {
        return super.getUUID(id + "." + OWNER);
    }

    public List<UUID> getStashMembers(Integer id) {
        return super.getUUIDList(id + "." + MEMBERS);
    }

    public List<ItemStack> getStashItems(Integer id) {
        List<ItemStack> items = new ArrayList<>();
        for (Integer i : super.getIntegerList(id + "." + ITEMS)) {
            items.add(super.getItemStack(id + "." + ITEMS + "." + i));
        }
        return items;
    }
}
