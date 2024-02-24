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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

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
    Stash.loadRecipe();
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
    User user = Server.getUser(e.getPlayer());

    if (Harvester.ITEM.equals(item)) {
      Harvester harv = new Harvester(this.getNewId(), block);
      this.machineByBlockByType.get(Machine.Type.HARVESTER).put(block, harv);
      this.usedIds.add(harv.getId());
      Server.getUser(e.getPlayer()).sendPluginMessage(Plugin.SURVIVAL,
          Component.text("Harvester placed", ExTextColor.PERSONAL));
    } else if (Stash.ITEM.equals(item)) {
      if (this.machineByBlockByType.get(Machine.Type.STASH).values().stream().map(
          s -> ((Stash) s).getOwner()).toList().contains(user.getUniqueId())) {
        user.sendPluginTDMessage(Plugin.SURVIVAL, "§sYou have already a stash");
        e.setCancelled(true);
        e.setBuild(false);
        return;
      }

      Stash stash = new Stash(this.getNewId(), block, e.getPlayer().getUniqueId(),
          new LinkedList<>());
      this.machineByBlockByType.get(Machine.Type.STASH).put(block, stash);
      this.usedIds.add(stash.getId());
      Server.getUser(e.getPlayer()).sendPluginTDMessage(Plugin.SURVIVAL, "§sStash placed");
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
    } else if (this.machineByBlockByType.get(Machine.Type.STASH).containsKey(block)) {
      Stash stash = ((Stash) this.machineByBlockByType.get(Machine.Type.STASH).get(block));

      if (stash.getItems().isEmpty()) {
        if (stash.getOwner().equals(user.getUniqueId())) {
          this.machineByBlockByType.get(Machine.Type.STASH).remove(block);
          this.file.removeMachine(stash);
          this.usedIds.remove(stash.getId());
          e.setDropItems(false);
          Server.dropItem(block.getLocation(), Stash.ITEM);
          sender.sendPluginTDMessage("§sStash destroyed");
        } else {
          e.setDropItems(false);
          e.setCancelled(true);
          sender.sendPluginTDMessage("§wThis is not your stash");
        }
      } else {
        e.setDropItems(false);
        e.setCancelled(true);
        sender.sendPluginTDMessage("§wStash must be empty before it can be destroyed");
      }
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
  public void onInteract(PlayerInteractEvent e) {
    Block block = e.getClickedBlock();

    if (block == null) {
      return;
    }

    Stash stash = (Stash) this.machineByBlockByType.get(Machine.Type.STASH).get(block);
    User user = Server.getUser(e.getPlayer());

    if (stash != null && !e.getPlayer().isSneaking()) {
      if (stash.getOwner().equals(user.getUniqueId()) || stash.getMembers()
          .contains(user.getUniqueId())) {
        stash.openInventoryFor(user);
        e.setCancelled(true);
      } else {
        user.sendPluginMessage(Plugin.SURVIVAL,
            Component.text("Access denied", ExTextColor.WARNING));
      }
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
