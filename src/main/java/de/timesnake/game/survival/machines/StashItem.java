package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.user.ExItemStack;

public class StashItem {

    private final ExItemStack item;
    private final int amount;

    public StashItem(ExItemStack item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    public ExItemStack getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }


}
