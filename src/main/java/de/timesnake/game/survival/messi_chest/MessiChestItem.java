/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.messi_chest;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class MessiChestItem {

  private final ExItemStack displayItem;
  private final ItemStack item;

  public MessiChestItem(ItemStack item) {
    this.item = item;

    this.displayItem = new ExItemStack(this.item).asQuantity(1);
    this.updateDisplayItem();
  }

  public ExItemStack getDisplayItem() {
    return displayItem;
  }

  public ItemStack getItem() {
    return item;
  }

  public void updateDisplayItem() {
    if (this.item.getAmount() < this.item.getMaxStackSize() || this.item.getMaxStackSize() <= 1) {
      this.displayItem.setLore("", "§9" + this.item.getAmount());
    } else if (this.item.getAmount() < this.item.getMaxStackSize() * 2) {
      this.displayItem.setLore("", "§9" + this.item.getAmount(),
          "§9" + this.item.getAmount() / this.item.getMaxStackSize() + "+ stack");
    } else {
      this.displayItem.setLore("", "§9" + this.item.getAmount(),
          "§9" + this.item.getAmount() / this.item.getMaxStackSize() + "+ stacks");
    }
  }

  public void add(ItemStack item) {
    this.item.setAmount(this.item.getAmount() + item.getAmount());
    this.updateDisplayItem();
  }

  public ItemStack remove(MessiChest messiChest, int amount) {
    int toRemove = Math.min(this.item.getAmount(), amount);
    ItemStack item = this.item.asQuantity(toRemove);
    this.item.setAmount(this.item.getAmount() - toRemove);

    if (this.item.getAmount() <= 0) {
      messiChest.removeItem(this);
    } else {
      this.updateDisplayItem();
    }

    return item;
  }

  public boolean isEmpty() {
    return this.item.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MessiChestItem stashItem)) {
      return false;
    }
    return Objects.equals(item, stashItem.item);
  }

  @Override
  public int hashCode() {
    return Objects.hash(item.asOne());
  }
}
