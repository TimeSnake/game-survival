package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.library.basic.util.chat.ChatColor;
import org.bukkit.inventory.ItemStack;

public class StashItem {

    private final ExItemStack displayItem;
    private final ItemStack item;

    public StashItem(ItemStack item) {
        this.item = item;

        this.displayItem = new ExItemStack(this.item);
        this.updateDisplayItem();
    }

    public ExItemStack getDisplayItem() {
        return displayItem;
    }

    public ItemStack getItem() {
        return item;
    }

    public void updateDisplayItem() {
        this.displayItem.setLore(ChatColor.BLUE + this.item.getAmount());
    }
}
