package de.timesnake.game.survival.privates;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.game.survival.hep.SurvivalHEP;
import de.timesnake.game.survival.player.SurvivalUser;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.library.extension.util.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PrivateBlockManger implements Listener {

    private PrivateBlocksFile file;
    private Map<Integer, PrivateBlock> blocks = new HashMap<>();


    public PrivateBlockManger() {
        this.file = new PrivateBlocksFile();
        for (Integer id : this.file.getIds()) {
            this.blocks.put(id, this.file.getPrivateBlock(id));
        }
    }

    public void savePrivateBlocksToFile() {
        this.file.resetPrivateBlocks();
        for (PrivateBlock block : this.blocks.values()) {
            this.file.addPrivateBlock(block);
        }
        this.file.save();
    }

    public Map<Integer, PrivateBlock> getBlocksWithId() {
        return this.blocks;
    }

    public Collection<PrivateBlock> getBlocks() {
        return this.blocks.values();
    }

    public void addBlock(PrivateBlock block) {
        blocks.put(block.getId(), block);
    }

    private Integer getNewId() {
        int id = 0;
        while (blocks.containsKey(id)) {
            id++;
        }
        return id;
    }

    public void addBlock(Block block, User user) {
        Sender sender = user.asSender(Plugin.PRIVATE_BLOCKS);
        sender.sendPluginMessage(ChatColor.PERSONAL + "Block is now private");
        PrivateBlock privateBlock;
        if (this.hasInventory(block)) {
            privateBlock = new PrivateInventoryBlock(this.getNewId(), block, user.getUniqueId(), null, false, null);
        } else {
            privateBlock = new PrivateBlock(this.getNewId(), block, user.getUniqueId(), null, false, null);
        }
        this.addBlock(privateBlock);
    }

    public boolean hasInventory(Block block) {
        return block.getState() instanceof InventoryHolder;
    }

    public PrivateBlock getBlock(Block block) {
        for (PrivateBlock privateBlock : this.blocks.values()) {
            if (privateBlock.getBlock().getLocation().equals(block.getLocation())) {
                return privateBlock;
            }
        }
        return null;
    }

    public PrivateInventoryBlock getBlock(Inventory inventory) {
        for (PrivateBlock block : this.blocks.values()) {
            if (block instanceof PrivateInventoryBlock) {
                if (((PrivateInventoryBlock) block).getInventory() == null) {
                    continue;
                }
                if (((PrivateInventoryBlock) block).getInventory().equals(inventory)) {
                    return (PrivateInventoryBlock) block;
                }
            }
        }
        return null;
    }

    public void removeBlock(PrivateBlock block, SurvivalUser user) {
        if (block instanceof PrivateBlock && this.blocks.containsValue(block)) {
            this.blocks.remove(block.getId());
            user.sendPluginMessage(Plugin.PRIVATE_BLOCKS, ChatColor.PERSONAL + "Selected block is now free");
        } else {
            user.sendPluginMessage(Plugin.PRIVATE_BLOCKS, ChatColor.WARNING + "Selected block is not private " + Chat.getMessageCode("E", 2000, Plugin.PRIVATE_BLOCKS));
            user.sendPluginMessage(Plugin.PRIVATE_BLOCKS, ChatColor.PERSONAL + "Please contact a supporter");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        User user = Server.getUser(e.getPlayer());
        Block block = e.getBlock();
        if (block.getState() instanceof InventoryHolder) {
            if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
                Block nearPrivateBlockToCreate = null;
                for (BlockFace blockFace : BlockFace.values()) {
                    Block nearBlock = block.getRelative(blockFace);
                    if (nearBlock.getType().equals(block.getType())) {
                        if (isInventoryPrivate(((InventoryHolder) nearBlock.getState()).getInventory().getHolder(), ((InventoryHolder) nearBlock.getState()).getInventory())) {
                            PrivateInventoryBlock nearPrivateBlock = (PrivateInventoryBlock) SurvivalServer.getPrivateBlockManger().getBlock(nearBlock);
                            assert nearPrivateBlock != null;
                            if (!user.getUniqueId().equals(nearPrivateBlock.getOwner()) || !user.hasPermission("survival.privateblocks.nearby"))
                                if (!user.hasPermission("survival.privateblocks.nearby.other")) {
                                    e.setCancelled(true);
                                    user.sendPluginMessage(Plugin.PRIVATE_BLOCKS, ChatColor.WARNING + "You can not place a chest nearby a foreign chest " + Chat.getMessageCode("H", 1907, Plugin.PRIVATE_BLOCKS));
                                    return;
                                }
                        } else {
                            nearPrivateBlockToCreate = nearBlock;
                        }
                    }
                }
                SurvivalServer.getPrivateBlockManger().addBlock(block, user);
                if (nearPrivateBlockToCreate != null) {
                    SurvivalServer.getPrivateBlockManger().addBlock(nearPrivateBlockToCreate, user);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        SurvivalUser user = (SurvivalUser) Server.getUser(e.getPlayer());
        Sender sender = user.asSender(Plugin.PRIVATE_BLOCKS);
        PrivateBlockManger manger = SurvivalServer.getPrivateBlockManger();
        PrivateBlock block = manger.getBlock(e.getBlock());
        if (block != null) {
            if (user.getUniqueId().equals(block.getOwner())) {
                if (sender.hasPermission("survival.privateblocks.destroy", 1802)) {
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Private-Block destroyed");
                    manger.removeBlock(block, user);
                }
            } else if (sender.hasPermission("survival.privateblocks.destroy.other")) {
                sender.sendPluginMessage(ChatColor.PERSONAL + "Private-Block destroyed");
                manger.removeBlock(block, user);
            } else {
                e.setCancelled(true);
                sender.sendMessage(SurvivalHEP.getMessageNotPrivateBlockOwner());
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (isInventoryPrivate(e.getInventory().getHolder(), e.getInventory())) {
            PrivateInventoryBlock block = SurvivalServer.getPrivateBlockManger().getBlock(e.getInventory());
            Player player = (Player) e.getPlayer();

            assert block != null;
            if (!block.getOwner().equals(player.getUniqueId()) || !player.hasPermission("survival.privateblocks.inventory")) {

                if (!block.getMembers().contains(player.getUniqueId()) || !player.hasPermission("survival.privateblocks.inventory")) {

                    if (!player.hasPermission("survival.privateblocks.inventory.other")) {
                        e.setCancelled(true);
                        player.sendMessage(SurvivalHEP.getMessageBlockIsPrivate());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent e) {
        if (isInventoryPrivate(e.getSource().getHolder(), e.getSource())) {
            if (!isInventoryPrivate(e.getDestination().getHolder(), e.getDestination())) {
                e.setCancelled(true);
            } else {
                PrivateInventoryBlock sourceBlock = SurvivalServer.getPrivateBlockManger().getBlock(e.getSource());
                PrivateInventoryBlock destinationBlock = SurvivalServer.getPrivateBlockManger().getBlock(e.getDestination());
                assert sourceBlock != null;
                assert destinationBlock != null;
                if (((sourceBlock.getOwner().equals(destinationBlock.getOwner()) && Bukkit.getPlayer(destinationBlock.getOwner()).hasPermission("survival.privateblocks.inventory")) || Bukkit.getPlayer(destinationBlock.getOwner()).hasPermission("survival.privateblocks.inventory.other"))) {
                    sourceBlock.setInventory(sourceBlock.getInventory());
                    destinationBlock.setInventory(destinationBlock.getInventory());
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().clear();
        e.setCancelled(true);
    }

    private static boolean isInventoryPrivate(InventoryHolder holder, Inventory inv) {
        if (holder instanceof Chest || holder instanceof DoubleChest || holder instanceof Beacon || holder instanceof BrewingStand || holder instanceof Dispenser || holder instanceof Dropper || holder instanceof Furnace || holder instanceof Hopper || holder instanceof HopperMinecart || holder instanceof StorageMinecart) {
            if (inv != null) {
                PrivateBlock block = SurvivalServer.getPrivateBlockManger().getBlock(inv);
                return block != null;
            }
        }
        return false;
    }
}
