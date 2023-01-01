/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractListener;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.game.survival.main.GameSurvival;
import de.timesnake.library.basic.util.chat.ExTextColor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class MachineManager implements Listener, UserInventoryInteractListener {

    private final MachinesFile file = new MachinesFile();

    private final HashMap<Machine.Type, HashMap<Block, Machine>> machineByBlockByType = new HashMap<>();

    private final Collection<Integer> usedIds = new ArrayList<>();

    public MachineManager() {
        Server.registerListener(this, GameSurvival.getPlugin());
        Server.getInventoryEventManager().addInteractListener(this, Crafter.ITEM);

        Harvester.loadRecipe();
        Crafter.loadRecipe();
        Stash.loadRecipe();
        Server.printText(Plugin.SURVIVAL, "Loaded machine recipes");

        Collection<Integer> ids = this.file.getMachineIds();
        this.usedIds.addAll(ids);

        for (Machine.Type type : Machine.Type.values()) {
            this.machineByBlockByType.put(type, new HashMap<>());
        }

        for (Integer id : ids) {
            Machine machine = this.file.getMachine(id);
            if (machine == null) {
                Server.printWarning(Plugin.MACHINES, "Can not load machine: " + id + " (null)");
                continue;
            }
            this.machineByBlockByType.get(machine.getType()).put(machine.getBlock(), machine);
        }

        Server.printText(Plugin.MACHINES, "Loaded machines");
    }

    public void saveMachinesToFile() {
        this.file.resetMachines();
        this.machineByBlockByType.values().forEach(m -> m.values().forEach(this.file::addMachine));
        Server.printText(Plugin.SURVIVAL, "Saved machines to file");
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
                user.sendPluginMessage(Plugin.SURVIVAL,
                        Component.text("You have already a stash", ExTextColor.WARNING));
                e.setCancelled(true);
                e.setBuild(false);
                return;
            }

            Stash stash = new Stash(this.getNewId(), block, e.getPlayer().getUniqueId(),
                    new LinkedList<>());
            this.machineByBlockByType.get(Machine.Type.STASH).put(block, stash);
            this.usedIds.add(stash.getId());
            Server.getUser(e.getPlayer()).sendPluginMessage(Plugin.SURVIVAL,
                    Component.text("Stash placed", ExTextColor.PERSONAL));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        User user = Server.getUser(e.getPlayer());
        Sender sender = user.asSender(Plugin.SURVIVAL);

        if (this.machineByBlockByType.get(Machine.Type.HARVESTER).containsKey(block)) {
            Harvester harv = (Harvester) this.machineByBlockByType.get(Machine.Type.HARVESTER)
                    .remove(block);
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
                    sender.sendPluginMessage(
                            Component.text("Stash destroyed", ExTextColor.PERSONAL));
                } else {
                    e.setDropItems(false);
                    e.setCancelled(true);
                    sender.sendPluginMessage(
                            Component.text("This is not your stash", ExTextColor.WARNING));
                }
            } else {
                e.setDropItems(false);
                e.setCancelled(true);
                sender.sendPluginMessage(
                        Component.text("Stash must be empty before it can be destroyed",
                                ExTextColor.WARNING));
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

    public HashMap<Machine.Type, HashMap<Block, Machine>> getMachineByBlockByType() {
        return machineByBlockByType;
    }
}
