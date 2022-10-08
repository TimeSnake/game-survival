/*
 * game-survival.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
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
