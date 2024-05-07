/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.chat;


import de.timesnake.library.basic.util.LogHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

  public static final Plugin SURVIVAL = new Plugin("Survival", "GSS",
      LogHelper.getLogger("Survival", Level.INFO));
  public static final Plugin MACHINES = new Plugin("Machine", "GSM",
      LogHelper.getLogger("Machine", Level.INFO));
  public static final Plugin REWARDS = new Plugin("Rewards", "GSR",
      LogHelper.getLogger("Rewards", Level.INFO));

  protected Plugin(String name, String code, Logger logger) {
    super(name, code);
  }

}
