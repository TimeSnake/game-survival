/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.player;

import de.timesnake.basic.bukkit.util.user.User;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SurvivalUser extends User {

  private Block selectedBlock;
  private Location deathLocation;

  public SurvivalUser(Player player) {
    super(player);
  }

  public Block getSelectedBlock() {
    return selectedBlock;
  }

  public void setSelectedBlock(Block block) {
    this.selectedBlock = block;
  }

  public boolean hasBlockSelected() {
    if (this.selectedBlock != null) {
      return true;
    }
    return false;
  }

  public void resetSelectedBlock() {
    this.selectedBlock = null;
  }

  public Location getDeathLocation() {
    return deathLocation;
  }

  public void setDeathLocation(Location deathLocation) {
    this.deathLocation = deathLocation;
  }

}
