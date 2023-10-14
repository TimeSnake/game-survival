/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.tools;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ShapedRecipe;

public class ItemMagnet extends Machine implements Listener {

  public static final String NAME = "§cMagnet";
  public static final ExItemStack ITEM = ExItemStack.getHashedIdItem(Material.HEART_OF_THE_SEA, "magnet")
      .setDisplayName(NAME)
      .setLore("§7Magnet", "§7Attracts items")
      .enchant()
      .immutable();
  public static final Integer RADIUS_XZ = 4;
  public static final Integer RADIUS_Y = 2;

  public static void loadRecipe() {
    ShapedRecipe stashRecipe = new ShapedRecipe(NamespacedKey.minecraft("magnet"), ItemMagnet.ITEM);

    stashRecipe.shape("ASA", "ERE", "AHA");

    stashRecipe.setIngredient('A', Material.AMETHYST_BLOCK);
    stashRecipe.setIngredient('S', Material.HEART_OF_THE_SEA);
    stashRecipe.setIngredient('E', Material.ECHO_SHARD);
    stashRecipe.setIngredient('R', Material.RABBIT_HIDE);
    stashRecipe.setIngredient('H', Material.HONEY_BLOCK);

    Bukkit.getServer().addRecipe(stashRecipe);
  }

  public ItemMagnet(Integer id, Block block) {
    super(id, block);
  }

  @Override
  public Type getType() {
    return Type.MAGNET;
  }


}
