package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import org.bukkit.Material;

import java.util.List;

public class Crafter {

    public static final String NAME = "§rPortable Crafting Table";

    public static final ExItemStack ITEM = new ExItemStack(Material.CRAFTING_TABLE, 1, NAME, List.of("§7Click to " +
            "open"));

}
