package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class Stash extends Machine {

    private static final List<Material> SMALL_STACKED_ITEMS = List.of(Material.SNOWBALL, Material.ENDER_PEARL, Material.ARMOR_STAND, Material.BUCKET, Material.ACACIA_SIGN, Material.BIRCH_SIGN, Material.CRIMSON_SIGN, Material.DARK_OAK_SIGN, Material.JUNGLE_SIGN, Material.OAK_SIGN, Material.SPRUCE_SIGN, Material.WARPED_SIGN);

    private static final ExItemStack up = new ExItemStack(Material.WHITE_WOOL, 0, "up");
    private static final ExItemStack down = new ExItemStack(Material.WHITE_WOOL, 9, "down");

    private final UUID owner;
    private final List<UUID> members;

    private final ExInventory inventory;
    private final LinkedHashMap<ExItemStack, StashItem> stashItemsOrderedByAmount = new LinkedHashMap<>();
    private final LinkedHashMap<ExItemStack, StashItem> stashItemsOrderedLexicographically = new LinkedHashMap<>();

    public Stash(Integer id, Block block, UUID owner) {
        super(id, block.getLocation());
        this.inventory = Server.createExInventory(6 * 9, "Stash");
        this.owner = owner;
        this.members = new ArrayList<>();
        this.updateInventory();
    }

    private void updateInventory() {
        this.inventory.getInventory().clear();
        this.inventory.setItemStack(up);
        this.inventory.setItemStack(down);

    }

    public ExInventory getInventory() {
        return inventory;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    @Override
    public Type getType() {
        return Type.STASH;
    }
}
