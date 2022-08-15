package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractListener;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.game.survival.main.GameSurvival;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MachineManager implements Listener, UserInventoryInteractListener {

    private final MachinesFile file = new MachinesFile();

    private final HashMap<Location, Harvester> harvesterLocations = new HashMap<>();
    private final HashMap<Location, Miner> minerLocations = new HashMap<>();

    private final Collection<Integer> usedIds = new ArrayList<>();

    public MachineManager() {
        Server.registerListener(this, GameSurvival.getPlugin());
        Server.getInventoryEventManager().addInteractListener(this, Crafter.ITEM);

        //harvester
        ShapedRecipe harvesterRecipe = new ShapedRecipe(NamespacedKey.minecraft("harvester"), Harvester.ITEM);

        harvesterRecipe.shape("EBE", "HDH", "EPE");

        harvesterRecipe.setIngredient('E', Material.EMERALD);
        harvesterRecipe.setIngredient('B', Material.LAVA_BUCKET);
        harvesterRecipe.setIngredient('H', Material.HOPPER);
        harvesterRecipe.setIngredient('D', Material.DIAMOND_AXE);
        harvesterRecipe.setIngredient('P', Material.STICKY_PISTON);

        Bukkit.getServer().addRecipe(harvesterRecipe);
        Server.printText(Plugin.MACHINES, "Loaded harvester recipe");

        //miner
        ShapedRecipe minerRecipe = new ShapedRecipe(NamespacedKey.minecraft("miner"), Miner.ITEM);

        minerRecipe.shape("EBE", "HDH", "EME");

        minerRecipe.setIngredient('E', Material.EMERALD);
        minerRecipe.setIngredient('B', Material.WATER_BUCKET);
        minerRecipe.setIngredient('H', Material.HOPPER);
        minerRecipe.setIngredient('D', Material.DIAMOND_PICKAXE);
        minerRecipe.setIngredient('M', Material.MAGMA_CREAM);

        Bukkit.getServer().addRecipe(minerRecipe);
        Server.printText(Plugin.MACHINES, "Loaded miner recipe");

        //craftingtable
        ShapedRecipe craftRecipe = new ShapedRecipe(NamespacedKey.minecraft("crafting"), Crafter.ITEM);

        craftRecipe.shape("ASD", "BZE", "CSF");

        craftRecipe.setIngredient('S', Material.SHULKER_SHELL);
        craftRecipe.setIngredient('Z', Material.CRAFTING_TABLE);
        craftRecipe.setIngredient('A', Material.ACACIA_LOG);
        craftRecipe.setIngredient('B', Material.BIRCH_LOG);
        craftRecipe.setIngredient('C', Material.DARK_OAK_LOG);
        craftRecipe.setIngredient('D', Material.JUNGLE_LOG);
        craftRecipe.setIngredient('E', Material.OAK_LOG);
        craftRecipe.setIngredient('F', Material.SPRUCE_LOG);

        Bukkit.getServer().addRecipe(craftRecipe);
        Server.printText(Plugin.MACHINES, "Loaded portable crafting table");

        //file
        Collection<Integer> ids = this.file.getMachineIds();
        Server.printText(Plugin.MACHINES, "Loading machines");

        for (Integer id : ids) {
            Machine machine = this.file.getMachine(id);
            if (machine == null) {
                Server.printError(Plugin.MACHINES, "Can not load machine: " + id + " (null)");
                continue;
            }
            if (machine.getType().equals(Machine.Type.HARVESTER)) {
                this.harvesterLocations.put(machine.getLocation(), (Harvester) machine);
                this.usedIds.add(machine.getId());
                Server.printText(Plugin.MACHINES, "Loaded harvester " + machine.getId());
            } else if (machine.getType().equals(Machine.Type.MINER)) {
                this.minerLocations.put(machine.getLocation(), (Miner) machine);
                this.usedIds.add(machine.getId());
                Server.printText(Plugin.MACHINES, "Loaded miner " + machine.getId());
            } else {
                Server.printError(Plugin.MACHINES, "Can not load machine: " + id + " (Unknown type)");
            }
        }

        this.generate();
    }

    public void saveMachinesToFile() {
        this.file.resetMachines();
        this.saveToFile(this.harvesterLocations.values());
        this.saveToFile(this.minerLocations.values());
    }

    private <M extends Machine> void saveToFile(Collection<M> machines) {
        for (Machine machine : machines) {
            this.file.addMachine(machine);
        }
    }

    private void generate() {
        new BukkitRunnable() {

            @Override
            public void run() {
                for (Miner miner : minerLocations.values()) {
                    miner.generate();
                }
            }
        }.runTaskTimer(GameSurvival.getPlugin(), 0, 60 * 20);
    }


    @EventHandler
    public void onStructureGrow(StructureGrowEvent e) {
        Block treeBlock = e.getLocation().getBlock();
        Location treeLoc = e.getLocation();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Harvester harvester : harvesterLocations.values()) {
                    if (harvester.isInRange(treeLoc)) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                harvester.fellTree();
                            }
                        }.runTaskLater(GameSurvival.getPlugin(), 20);
                    }
                }
            }
        }.runTaskAsynchronously(GameSurvival.getPlugin());
    }


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType().equals(Material.DROPPER)) {
            Block block = e.getBlock();
            org.bukkit.inventory.ItemStack item = e.getItemInHand();
            if (item.hasItemMeta()) {
                if (item.getItemMeta() != null) {
                    String tag = item.getItemMeta().getLocalizedName();
                    if (tag == null) {
                        return;
                    }
                    Location loc = block.getLocation();
                    if (tag.equals(String.valueOf(Harvester.ITEM.getId())) && block.getState() instanceof InventoryHolder) {
                        Harvester harv = new Harvester(this.getNewId(), loc);
                        this.harvesterLocations.put(loc, harv);
                        this.usedIds.add(harv.getId());
                        Server.getUser(e.getPlayer()).sendPluginMessage(Plugin.SURVIVAL,
                                Component.text("Harvester placed", ExTextColor.PERSONAL));
                    } else if (tag.equals(String.valueOf(Miner.ITEM.getId()))) {
                        Miner miner = new Miner(this.getNewId(), loc);
                        this.minerLocations.put(loc, miner);
                        this.usedIds.add(miner.getId());
                        Server.getUser(e.getPlayer()).sendPluginMessage(Plugin.SURVIVAL,
                                Component.text(" Miner placed", ExTextColor.PERSONAL));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(Material.DROPPER)) {
            Block block = e.getBlock();
            Location loc = e.getBlock().getLocation();
            Sender sender = Server.getUser(e.getPlayer()).asSender(Plugin.SURVIVAL);
            if (this.harvesterLocations.containsKey(loc)) {
                Harvester harv = this.harvesterLocations.get(loc);
                this.usedIds.remove(harv.getId());
                this.harvesterLocations.remove(block.getLocation());
                e.setDropItems(false);
                Server.dropItem(loc, Harvester.ITEM);
                sender.sendPluginMessage(Component.text("Harvester destroyed", ExTextColor.PERSONAL));
            } else if (this.minerLocations.containsKey(loc)) {
                Miner miner = this.minerLocations.get(loc);
                this.usedIds.remove(miner.getId());
                this.minerLocations.remove(loc);
                e.setDropItems(false);
                Server.dropItem(loc, Miner.ITEM);
                sender.sendPluginMessage(Component.text("Miner destroyed", ExTextColor.PERSONAL));
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
}
