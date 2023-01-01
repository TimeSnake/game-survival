/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

public class Crafter {

    public static final String NAME = "§rPortable Crafting Table";
    public static final ExItemStack ITEM = ExItemStack.getHashedIdItem(Material.CRAFTING_TABLE, "port_craft_table")
            .setDisplayName(NAME).setLore("§7Click to open or place");

    public static void loadRecipe() {
        ShapedRecipe craftRecipe = new ShapedRecipe(NamespacedKey.minecraft("crafting"), Crafter.ITEM);

        craftRecipe.shape("ASD", "BZE", "CSF");

        craftRecipe.setIngredient('S', Material.SHULKER_SHELL);
        craftRecipe.setIngredient('Z', Material.CRAFTING_TABLE);
        craftRecipe.setIngredient('A', Material.ACACIA_LOG);
        craftRecipe.setIngredient('B', Material.BIRCH_LOG);
        craftRecipe.setIngredient('C', Material.DARK_OAK_LOG);
        craftRecipe.setIngredient('D', Material.JUNGLE_LOG);
        craftRecipe.setIngredient('E', Material.OAK_LOG);
        craftRecipe.setIngredient('F', Material.SPRUCE_LOG);

        Bukkit.getServer().addRecipe(craftRecipe);
    }

}
