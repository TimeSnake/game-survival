/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.machines;


import org.bukkit.block.Block;

public abstract class Machine {

  protected final Integer id;
  protected final Block block;

  public Machine(Integer id, Block block) {
    this.id = id;
    this.block = block;
  }

  public Integer getId() {
    return id;
  }

  public Block getBlock() {
    return block;
  }

  public abstract Machine.Type getType();

  public enum Type {
    HARVESTER,
    MINER,
    STASH;
  }


}
