package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.game.survival.main.GameSurvival;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Harvester extends Machine implements Listener {

    public static final String NAME = "§rHarvester";

    public static final ExItemStack ITEM = new ExItemStack(Material.DROPPER, 1, NAME, List.of("§7Harvester", "§7Harvests trees automatic"));
    public static final Integer RADIUS = 5;

    private static final ArrayList<Material> woodTypes = new ArrayList<>(List.of(Material.ACACIA_LOG, Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.DARK_OAK_LOG));

    private BukkitTask task;

    public Harvester(Integer id, Location location) {
        super(id, location);
    }

    public Machine.Type getType() {
        return Type.HARVESTER;
    }

    boolean isInRange(Location loc) {
        int xDiff = this.location.getBlockX() - loc.getBlockX();
        int yDiff = this.location.getBlockY() - loc.getBlockY();
        int zDiff = this.location.getBlockZ() - loc.getBlockZ();

        return (xDiff < RADIUS && xDiff > -RADIUS) && yDiff == 0 && (zDiff < RADIUS && zDiff > -RADIUS);
    }

    void fellTree() {

        if (task != null) {
            task.cancel();
        }

        task = new BukkitRunnable() {

            final boolean yFelled = true;
            Integer x = location.getBlockX() - RADIUS;
            int y = location.getBlockY();
            int z = location.getBlockZ() - RADIUS;

            final int xEnd = location.getBlockX() + RADIUS;
            final int zEnd = location.getBlockZ() + RADIUS;

            @Override
            public void run() {
                if (yFelled) {
                    if (x <= xEnd) {
                        if (z <= zEnd) {
                            Location loc = new Location(location.getWorld(), x, y, z);
                            if (isFellable(loc)) {
                                fellBlock(loc);
                                z++;
                            } else {
                                z++;
                                this.run();
                            }
                        } else {
                            z = location.getBlockZ() - RADIUS;
                            x++;
                        }
                    } else {
                        y++;
                        x = location.getBlockX() - RADIUS;
                        z = location.getBlockZ() - RADIUS;
                    }

                }
            }
        }.runTaskTimer(GameSurvival.getPlugin(), 0, 20);
    }

    private boolean isFellable(Location loc) {
        return woodTypes.contains(loc.getBlock().getType());
    }

    private void fellBlock(Location loc) {
        Block block = loc.getBlock();

        if (woodTypes.contains(block.getType())) {
            Material type = block.getType();
            for (org.bukkit.inventory.ItemStack item : this.getInventory()) {
                if (item == null || (item.getAmount() < 64 && item.getType().equals(type))) {
                    this.getInventory().addItem(new org.bukkit.inventory.ItemStack(type));
                    block.setType(Material.AIR);
                    loc.getWorld().playSound(loc, Sound.BLOCK_WOOD_BREAK, 1F, 1F);
                    loc.getWorld().playEffect(loc, Effect.STEP_SOUND, 17);
                    return;
                }
            }
            // inventory full
            this.task.cancel();
        }
    }

    private Inventory getInventory() {
        return ((InventoryHolder) super.getLocation().getBlock().getState()).getInventory();
    }

}
