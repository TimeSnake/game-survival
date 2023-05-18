/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.world;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SpawnProtection implements Listener {

  public static final int RADIUS = 10;

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
    if (e.getEntity() instanceof Player) {
      e.setCancelled(SpawnProtection.isSpawnArea(e.getEntity().getLocation()));
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player) {
      e.setCancelled(SpawnProtection.isSpawnArea(e.getEntity().getLocation()));
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    if (SpawnProtection.isSpawnArea(e.getBlock().getLocation())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent e) {
    if (SpawnProtection.isSpawnArea(e.getBlock().getLocation())) {
      e.setCancelled(true);
    }
  }


  @EventHandler
  public void onEntityExplode(EntityExplodeEvent e) {
    if (e.getEntityType().equals(EntityType.CREEPER) || e.getEntityType().equals(EntityType.WITHER)
        || e.getEntityType().equals(EntityType.GHAST)) {
      e.blockList().clear();
    } else {
      e.setCancelled(true);
    }
  }

  public static boolean isSpawnArea(Location location) {
    Location spawn = location.getWorld().getSpawnLocation();
    if (location.getX() <= spawn.getBlockX() + RADIUS
        && location.getZ() <= spawn.getBlockZ() + RADIUS
        && location.getX() >= spawn.getBlockX() - RADIUS
        && location.getZ() >= spawn.getBlockZ() - RADIUS) {
      return true;
    }
    return false;
  }

}
