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
        if (e.getEntityType().equals(EntityType.CREEPER) || e.getEntityType().equals(EntityType.WITHER) || e.getEntityType().equals(EntityType.GHAST)) {
            e.blockList().clear();
        } else {
            e.setCancelled(true);
        }
    }

    public static boolean isSpawnArea(Location location) {
        Location spawn = location.getWorld().getSpawnLocation();
        if (location.getX() <= spawn.getBlockX() + RADIUS && location.getZ() <= spawn.getBlockZ() + RADIUS && location.getX() >= spawn.getBlockX() - RADIUS && location.getZ() >= spawn.getBlockZ() - RADIUS) {
            return true;
        }
        return false;
    }

}
