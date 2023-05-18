/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.rewards;

import org.bukkit.inventory.ItemStack;

public interface Reward {

  String getName();

  int getGoal();

  ItemStack[] getPrizes();
}
