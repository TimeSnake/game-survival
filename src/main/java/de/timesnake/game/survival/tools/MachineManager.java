/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.tools;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryInteractListener;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.game.survival.main.GameSurvival;
import de.timesnake.game.survival.messi_chest.MessiChest;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.library.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MachineManager implements Listener, UserInventoryInteractListener {

  private final Logger logger = LogManager.getLogger("survival.machine.manager");

  private final MachinesFile file = new MachinesFile();

  private final HashMap<Machine.Type, HashMap<Block, Machine>> machineByBlockByType = new HashMap<>();

  private final Collection<Integer> usedIds = new ArrayList<>();

  public MachineManager() {
    Server.registerListener(this, GameSurvival.getPlugin());
    Server.getInventoryEventManager().addInteractListener(this, Crafter.ITEM);

    Harvester.loadRecipe();
    Crafter.loadRecipe();
    ItemMagnet.loadRecipe();
    this.logger.info("Loaded machine recipes");

    Collection<Integer> ids = this.file.getMachineIds();
    this.usedIds.addAll(ids);

    for (Machine.Type type : Machine.Type.values()) {
      this.machineByBlockByType.put(type, new HashMap<>());
    }

    for (Integer id : ids) {
      Machine machine = this.file.getMachine(id);
      if (machine == null) {
        this.logger.warn("Can not load machine: {} (null)", id);
        continue;
      }
      if (machine instanceof Stash stash) {
        MessiChest chest = SurvivalServer.getMessiChestManager().createMessiChest(stash.getOwner(), stash.getBlock());
        stash.getMembers().forEach(chest::addMember);
        stash.getItems().forEach(item -> chest.addItem(null, item.getItem()));
        chest.updateInventories();
        this.file.removeMachine(stash);
        this.logger.info("Transformed stash {} to messi chest {}", stash.getId(), chest.getId());
        continue;
      }

      this.machineByBlockByType.get(machine.getType()).put(machine.getBlock(), machine);
    }

    this.logger.info("Loaded machines");
  }

  public void saveMachinesToFile() {
    this.file.resetMachines();
    this.machineByBlockByType.values().forEach(m -> m.values().forEach(this.file::addMachine));
    this.logger.info("Saved machines to file");
  }

  @EventHandler
  public void onStructureGrow(StructureGrowEvent e) {
    Location treeLoc = e.getLocation();
    Server.runTaskAsynchrony(() -> {
      for (Machine machine : this.machineByBlockByType.get(Machine.Type.HARVESTER).values()) {
        Harvester harvester = ((Harvester) machine);
        if (harvester.isInRange(treeLoc)) {
          new BukkitRunnable() {
            @Override
            public void run() {
              harvester.fellTree();
            }
          }.runTaskLater(GameSurvival.getPlugin(), 20);
        }
      }
    }, GameSurvival.getPlugin());
  }


  @EventHandler
  public void onBlockPlace(BlockPlaceEvent e) {
    Block block = e.getBlock();
    ExItemStack item = ExItemStack.getItem(e.getItemInHand(), true);

    if (Harvester.ITEM.equals(item)) {
      Harvester harv = new Harvester(this.getNewId(), block);
      this.machineByBlockByType.get(Machine.Type.HARVESTER).put(block, harv);
      this.usedIds.add(harv.getId());
      Server.getUser(e.getPlayer()).sendPluginMessage(Plugin.SURVIVAL,
          Component.text("Harvester placed", ExTextColor.PERSONAL));
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    Block block = e.getBlock();
    User user = Server.getUser(e.getPlayer());
    Sender sender = user.asSender(Plugin.SURVIVAL);

    if (this.machineByBlockByType.get(Machine.Type.HARVESTER).containsKey(block)) {
      Harvester harv = (Harvester) this.machineByBlockByType.get(Machine.Type.HARVESTER).remove(block);
      this.file.removeMachine(harv);
      this.usedIds.remove(harv.getId());
      e.setDropItems(false);
      Server.dropItem(block.getLocation(), Harvester.ITEM);
      sender.sendPluginMessage(Component.text("Harvester destroyed", ExTextColor.PERSONAL));
    }
  }

  private Integer getNewId() {
    Integer id = 0;
    while (this.usedIds.contains(id)) {
      id++;
    }
    this.usedIds.add(id);
    return id;
  }

  @Override
  public void onUserInventoryInteract(UserInventoryInteractEvent e) {
    if (e.getClickedItem().equals(Crafter.ITEM)) {
      e.getUser().getPlayer().openWorkbench(null, true);
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPistonPush(BlockPistonExtendEvent e) {
    for (HashMap<Block, Machine> m : this.machineByBlockByType.values()) {
      for (Block key : e.getBlocks()) {
        if (m.containsKey(key)) {
          e.setCancelled(true);
          return;
        }
      }
    }
  }

  @EventHandler
  public void onPistonPush(BlockPistonRetractEvent e) {
    for (HashMap<Block, Machine> m : this.machineByBlockByType.values()) {
      for (Block key : e.getBlocks()) {
        if (m.containsKey(key)) {
          e.setCancelled(true);
          return;
        }
      }
    }
  }

  @EventHandler
  public void onItemDrop(BlockDropItemEvent e) {
    this.handleItemDrop(e.getBlock().getLocation(), e.getItems());
  }

  @EventHandler
  public void onItemDrop(EntityDropItemEvent e) {
    this.handleItemDrop(e.getItemDrop().getLocation(), List.of(e.getItemDrop()));
  }

  private void handleItemDrop(Location location, List<Item> items) {
    for (Player p : location.getNearbyPlayers(ItemMagnet.RADIUS_XZ, ItemMagnet.RADIUS_Y)) {
      User user = Server.getUser(p);
      if (user.contains(ItemMagnet.ITEM)) {
        Vector v = user.getLocation().toVector().subtract(location.toVector()).normalize().multiply(0.3);
        items.forEach(i -> i.setVelocity(v));
        Server.runTaskLaterSynchrony(() -> items.forEach(i -> {
          if (!i.isDead()) {
            i.setVelocity(v);
          }
        }), 5, GameSurvival.getPlugin());

      }
    }
  }

  public HashMap<Machine.Type, HashMap<Block, Machine>> getMachineByBlockByType() {
    return machineByBlockByType;
  }
}
