/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.survival.rewards;

import org.bukkit.inventory.ItemStack;

public interface Reward {

    String getName();

    int getGoal();

    ItemStack[] getPrizes();
}
