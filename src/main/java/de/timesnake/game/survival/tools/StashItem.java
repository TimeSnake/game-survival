/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.tools;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.library.chat.ChatColor;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public class StashItem {

  private final ExItemStack displayItem;
  private final ItemStack item;
  private boolean empty;

  public StashItem(ItemStack item) {
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
      this.displayItem.setLore("", ChatColor.BLUE + this.item.getAmount());
    } else if (this.item.getAmount() < this.item.getMaxStackSize() * 2) {
      this.displayItem.setLore("", ChatColor.BLUE + this.item.getAmount(),
          ChatColor.BLUE + this.item.getAmount() / this.item.getMaxStackSize() + "+ stack");
    } else {
      this.displayItem.setLore("", ChatColor.BLUE + this.item.getAmount(),
          ChatColor.BLUE + this.item.getAmount() / this.item.getMaxStackSize() + "+ stacks");
    }
  }

  public void add(ItemStack item) {
    this.item.setAmount(this.item.getAmount() + item.getAmount());
    this.updateDisplayItem();
  }

  public ItemStack remove(int amount) {
    int toRemove = Math.min(this.item.getAmount(), amount);
    if (this.item.getAmount() - toRemove <= 0) {
      this.empty = true;
    } else {
      this.item.setAmount(this.item.getAmount() - toRemove);
    }
    this.updateDisplayItem();
    return this.item.asQuantity(toRemove);
  }

  public boolean stackable(ItemStack item) {
    return item != null && this.item.asOne().equals(item.asOne());
  }

  public boolean isEmpty() {
    return empty;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StashItem stashItem)) {
      return false;
    }
    return Objects.equals(item, stashItem.item);
  }

  @Override
  public int hashCode() {
    return Objects.hash(item.asOne());
  }
}
