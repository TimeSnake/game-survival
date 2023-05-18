/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.game.survival.main.GameSurvival;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Harvester extends Machine implements Listener {

  public static final String NAME = "§rHarvester";
  public static final ExItemStack ITEM = ExItemStack.getHashedIdItem(Material.DROPPER, "harvester")
      .setDisplayName(NAME).setLore("§7Harvester", "§7Harvests trees automatic").immutable();
  public static final Integer RADIUS = 5;

  public static void loadRecipe() {
    ShapedRecipe harvesterRecipe = new ShapedRecipe(NamespacedKey.minecraft("harvester"),
        Harvester.ITEM);

    harvesterRecipe.shape("EBE", "HDH", "EPE");

    harvesterRecipe.setIngredient('E', Material.EMERALD);
    harvesterRecipe.setIngredient('B', Material.LAVA_BUCKET);
    harvesterRecipe.setIngredient('H', Material.HOPPER);
    harvesterRecipe.setIngredient('D', Material.DIAMOND_AXE);
    harvesterRecipe.setIngredient('P', Material.STICKY_PISTON);

    Bukkit.getServer().addRecipe(harvesterRecipe);
  }

  private static final ArrayList<Material> woodTypes = new ArrayList<>(List.of(Material.ACACIA_LOG,
      Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
      Material.DARK_OAK_LOG));

  private BukkitTask task;

  public Harvester(Integer id, Block block) {
    super(id, block);
  }

  public Machine.Type getType() {
    return Type.HARVESTER;
  }

  boolean isInRange(Location loc) {
    int xDiff = this.block.getX() - loc.getBlockX();
    int yDiff = this.block.getY() - loc.getBlockY();
    int zDiff = this.block.getZ() - loc.getBlockZ();

    return (xDiff < RADIUS && xDiff > -RADIUS) && yDiff == 0 && (zDiff < RADIUS && zDiff > -RADIUS);
  }

  void fellTree() {

    if (task != null) {
      task.cancel();
    }

    task = new BukkitRunnable() {

      final boolean yFelled = true;
      final int xEnd = block.getX() + RADIUS;
      final int zEnd = block.getZ() + RADIUS;
      Integer x = block.getX() - RADIUS;
      int y = block.getY();
      int z = block.getZ() - RADIUS;

      @Override
      public void run() {
        if (yFelled) {
          if (x <= xEnd) {
            if (z <= zEnd) {
              Location loc = new Location(block.getWorld(), x, y, z);
              if (isFellable(loc)) {
                fellBlock(loc);
                z++;
              } else {
                z++;
                this.run();
              }
            } else {
              z = block.getZ() - RADIUS;
              x++;
            }
          } else {
            y++;
            x = block.getX() - RADIUS;
            z = block.getZ() - RADIUS;
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
    return ((InventoryHolder) super.getBlock().getState()).getInventory();
  }

}
