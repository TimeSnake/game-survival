/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.messi_chest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.game.survival.main.GameSurvival;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.library.basic.util.GsonFile;
import de.timesnake.library.basic.util.MultiKeyMap;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class MessiChest implements Listener {

  public static final ExItemStack ITEM = ExItemStack.getHashedIdItem(Material.LODESTONE,
      "messi_chest").setDisplayName("§6Messi Chest");

  public static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(MessiChest.class, new MessiChestAdapter())
      .serializeNulls()
      .create();

  public static MessiChest createFromJsonFile(File file) {
    return new GsonFile(file, GSON).read(MessiChest.class);
  }

  static Integer convertItemToKeyItem(ItemStack item) {
    return item.asOne().hashCode();
  }

  private transient final Logger itemLogger;

  private final int id;

  private final UUID owner;
  private final List<UUID> members;

  private final Block block;

  final transient MultiKeyMap<ExItemStack, Integer, MessiChestItem> itemByDisplayItemOrHash = new MultiKeyMap<>();
  final transient Set<Player> currentViewers = new HashSet<>();

  private final MessiChestModdedView moddedView;
  final MessiChestVanillaView vanillaView;

  public MessiChest(int id, UUID owner, List<UUID> memberUuids, Block block, List<MessiChestItem> items) {
    this.id = id;
    this.itemLogger = LogManager.getLogger("messi-chest.items");
    this.owner = owner;
    this.members = new ArrayList<>(memberUuids);

    this.block = block;

    for (MessiChestItem item : items) {
      this.itemByDisplayItemOrHash.put(item.getDisplayItem(), convertItemToKeyItem(item.getItem()), item);
    }

    this.moddedView = new MessiChestModdedView(this);
    this.vanillaView = new MessiChestVanillaView(this);

    Bukkit.getPluginManager().registerEvents(this, GameSurvival.getPlugin());
    this.updateInventories();
  }

  public int getId() {
    return id;
  }

  public UUID getOwner() {
    return owner;
  }

  public List<UUID> getMembers() {
    return members;
  }

  public Block getBlock() {
    return block;
  }

  public Collection<MessiChestItem> getItems() {
    return this.itemByDisplayItemOrHash.values();
  }

  public void saveToFile(Path folderPath) {
    File file = folderPath.resolve("chest_" + this.id + ".json").toFile();
    try {
      FileUtils.createParentDirectories(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    new GsonFile(file, GSON).write(this);
  }

  public boolean deleteFile(Path folderPath) {
    File file = folderPath.resolve("chest_" + this.id + ".json").toFile();

    if (file.exists()) {
      return file.delete();
    }

    return false;
  }

  public boolean openInventoryFor(Player player) {
    if (!player.getUniqueId().equals(this.owner) && !this.members.contains(player.getUniqueId())) {
      return false;
    }

    if (SurvivalServer.getMessiChestManager().hasPlayerModdedViewEnabled(player.getUniqueId())) {
      this.moddedView.openInventoryFor(player);
    } else {
      this.vanillaView.openInventoryFor(player);
    }
    return true;
  }

  public synchronized void addItem(@Nullable Player player, @Nullable ItemStack... items) {
    for (ItemStack item : items) {
      if (item == null || item.isEmpty()) {
        continue;
      }

      MessiChestItem stashItem = this.itemByDisplayItemOrHash.get2(convertItemToKeyItem(item));

      if (stashItem != null && !stashItem.isEmpty()) {
        stashItem.add(item);
      } else {
        stashItem = new MessiChestItem(item);
        this.itemByDisplayItemOrHash.put(stashItem.getDisplayItem(),
            convertItemToKeyItem(stashItem.getItem()), stashItem);
      }

      this.onItemAdd(player, item);
    }
  }

  public synchronized void updateInventories() {
    this.vanillaView.updateItems();
    this.broadcastUpdate();
  }

  private void broadcastUpdate() {
    for (Player player : this.currentViewers) {
      player.updateInventory();
    }
  }

  boolean onInteract(Player player, InteractAction action) {
    if (action instanceof AddAction addAction) {
      this.addItem(player, addAction.item().asQuantity(addAction.amount()));
      if (addAction.sourceSlot() == -1) {
        player.setItemOnCursor(addAction.item().asQuantity(addAction.item().getAmount() - addAction.amount()));
      } else {
        player.getInventory().setItem(addAction.sourceSlot(),
            addAction.item().asQuantity(addAction.item().getAmount() - addAction.amount()));
      }
      return true;
    } else if (action instanceof TakeToCursorAction takeToCursorAction) {
      MessiChestItem stashItem = this.itemByDisplayItemOrHash.get1(takeToCursorAction.clickedItem());
      if (stashItem == null) {
        return false;
      }
      ItemStack item = stashItem.remove(this, takeToCursorAction.amount());
      this.addItem(player, player.getItemOnCursor());
      player.setItemOnCursor(item);
      this.onItemRemove(player, item);
      return true;
    } else if (action instanceof MoveToPlayerInventoryAction moveToPlayerInventoryAction) {
      MessiChestItem messiChestItem = this.itemByDisplayItemOrHash.get1(moveToPlayerInventoryAction.clickedItem());
      if (messiChestItem == null) {
        return false;
      }
      ItemStack item = messiChestItem.remove(this, moveToPlayerInventoryAction.amount());
      Map<Integer, ItemStack> items = player.getInventory().addItem(item);
      if (!items.isEmpty()) {
        this.addItem(player, items.get(0));
        this.onItemRemove(player, item.asQuantity(item.getAmount() - items.get(0).getAmount()));
      } else {
        this.onItemRemove(player, item);
      }
      return true;
    }
    return false;
  }

  public void onItemAdd(@Nullable Player player, @NotNull ItemStack item) {
    this.itemLogger.info("messi-chest {}: added {} {} by {}", this.id, item.getAmount(),
        item.getType().name().toLowerCase(), player != null ? player.getName() : "system");
  }

  public void onItemRemove(@NotNull Player player, @NotNull ItemStack item) {
    this.itemLogger.info("messi-chest {}: removed {} {} by {}", this.id,
        item.getAmount(), item.getType().name().toLowerCase(),
        player.getName());
  }

  protected synchronized void removeItem(@NotNull MessiChestItem item) {
    this.itemByDisplayItemOrHash.remove(item.getDisplayItem(),
        convertItemToKeyItem(item.getItem()));
  }

  public boolean addMember(UUID memberUuid) {
    return this.members.add(memberUuid);
  }

  public boolean removeMember(UUID memberUuid) {
    if (this.members.remove(memberUuid)) {
      Player player = Bukkit.getPlayer(memberUuid);
      if (player != null && this.currentViewers.contains(player)) {
        player.closeInventory();
        this.currentViewers.remove(player);
      }
      return true;
    }
    return false;
  }

  private static class InteractAction {

  }

  static class AddAction extends InteractAction {

    private final ItemStack item;
    private final int amount;
    private final int sourceSlot;

    AddAction(ItemStack item, int amount, int sourceSlot) {
      this.item = item;
      this.amount = amount;
      this.sourceSlot = sourceSlot;
    }

    public ItemStack item() {
      return item;
    }

    public int amount() {
      return amount;
    }

    public int sourceSlot() {
      return sourceSlot;
    }
  }

  static class TakeToCursorAction extends InteractAction {

    private final ExItemStack clickedItem;
    private final int amount;

    TakeToCursorAction(ExItemStack clickedItem, int amount) {
      this.clickedItem = clickedItem;
      this.amount = amount;
    }

    public ExItemStack clickedItem() {
      return clickedItem;
    }

    public int amount() {
      return amount;
    }
  }

  static class MoveToPlayerInventoryAction extends InteractAction {

    private final ExItemStack clickedItem;
    private final int amount;

    MoveToPlayerInventoryAction(ExItemStack clickedItem, int amount) {
      this.clickedItem = clickedItem;
      this.amount = amount;
    }

    public ExItemStack clickedItem() {
      return clickedItem;
    }

    public int amount() {
      return amount;
    }
  }


}
