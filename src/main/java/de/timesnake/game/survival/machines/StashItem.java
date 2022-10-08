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
import de.timesnake.library.basic.util.chat.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

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
        if (this == o) return true;
        if (!(o instanceof StashItem stashItem)) return false;
        return Objects.equals(item, stashItem.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item.asOne());
    }
}
