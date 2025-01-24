/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.messi_chest;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.game.survival.main.GameSurvival;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MessiChestVanillaView implements Listener {

  private static final ExItemStack UP = new ExItemStack(Material.DETECTOR_RAIL)
      .setDisplayName("§9Up")
      .setSlot(8)
      .immutable();

  private static final ExItemStack PAGE = new ExItemStack(Material.PAPER)
      .setDisplayName("§9Page 0")
      .setSlot(17);

  private static final ExItemStack DOWN = new ExItemStack(Material.DETECTOR_RAIL)
      .setDisplayName("§9Down")
      .setSlot(26)
      .immutable();

  private static final ExItemStack ORDER = new ExItemStack(Material.SPYGLASS)
      .setDisplayName("§9Order")
      .setSlot(35);

  private static final ExItemStack ID = new ExItemStack(Material.PAPER)
      .setDisplayName("§9Messi Chest ID")
      .setSlot(44);

  private final MessiChest chest;

  private OrderType orderType = OrderType.AMOUNT;
  private final ExItemStack orderItem = ORDER.cloneWithId()
      .setLore("§7" + this.orderType.getDisplayName());

  private int maxPage = 0;

  private final Map<Integer, InventoryPage> pageByNumber = new HashMap<>();

  public MessiChestVanillaView(MessiChest chest) {
    this.chest = chest;
    this.pageByNumber.put(0, new InventoryPage(0));
    Bukkit.getPluginManager().registerEvents(this, GameSurvival.getPlugin());
  }

  public synchronized void updateItems() {
    HashMap<Integer, MessiChestItem> itemBySlot = new HashMap<>();
    InventoryPage currentPage = this.pageByNumber.get(0);

    List<MessiChestItem> items = new ArrayList<>(this.chest.getItems());

    items.sort(this.orderType.getComparator());

    int slot = 0;
    for (MessiChestItem item : items) {
      if (slot % 9 == 7) {
        slot += 2;
      }

      if (slot >= 6 * 9) {
        currentPage.update(itemBySlot);
        itemBySlot = new HashMap<>();

        slot = 0;
        currentPage = this.pageByNumber.computeIfAbsent(currentPage.getNumber() + 1,
            InventoryPage::new);
      }

      itemBySlot.put(slot, item);
      slot++;
    }

    currentPage.update(itemBySlot);

    this.maxPage = currentPage.getNumber();
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    if (!this.pageByNumber.containsValue(e.getView().getTopInventory().getHolder())) {
      return;
    }

    if (!e.getView().getBottomInventory().equals(e.getClickedInventory())) {
      return;
    }

    InventoryAction action = e.getAction();

    Player player = (Player) e.getWhoClicked();
    ItemStack item = e.getCurrentItem();

    if (item != null && action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
      e.setCancelled(true);
      this.chest.onInteract(player, new MessiChest.AddAction(item, item.getAmount(), e.getSlot()));
      this.updateItems();
      MessiChestVanillaView.this.chest.updateInventories();
    }
  }

  @EventHandler
  public void onInventoryDrag(InventoryDragEvent e) {
    if (!this.pageByNumber.containsValue(e.getInventory().getHolder())) {
      return;
    }

    if (e.getRawSlots().stream().anyMatch(s -> s < 6 * 9)) {
      e.setCancelled(true);
      e.setResult(Event.Result.DENY);
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent e) {
    this.chest.currentViewers.remove(((Player) e.getPlayer()));
  }

  public void openInventoryFor(Player player) {
    player.openInventory(this.pageByNumber.get(0).getInventory());
    this.chest.currentViewers.add(player);
  }

  public enum OrderType {
    AMOUNT("Amount",
        Comparator.comparingInt((MessiChestItem i) -> i.getItem().getAmount()).reversed()),
    NAME("Name", Comparator.comparing(i -> i.getItem().getType().name()));

    private final String displayName;

    private final Comparator<MessiChestItem> comparator;

    OrderType(String displayName, Comparator<MessiChestItem> comparator) {
      this.displayName = displayName;
      this.comparator = comparator;
    }

    public String getDisplayName() {
      return displayName;
    }

    public Comparator<MessiChestItem> getComparator() {
      return this.comparator;
    }
  }

  private boolean tryHandleActionItem(Player player, ExItemStack item, int number) {
    if (item.equals(UP)) {
      if (number > 0) {
        player.openInventory(this.pageByNumber.get(number - 1).getInventory());
      }
    } else if (item.equals(DOWN)) {
      if (number < this.maxPage) {
        player.openInventory(this.pageByNumber.get(number + 1).getInventory());
      }
    } else if (item.equals(ORDER)) {
      this.orderType = OrderType.values()[(this.orderType.ordinal() + 1)
                                          % OrderType.values().length];
      this.orderItem.setLore("§7" + this.orderType.getDisplayName());
      this.updateItems();
      MessiChestVanillaView.this.chest.updateInventories();
    } else {
      return false;
    }

    return true;
  }

  public class InventoryPage implements Listener, InventoryHolder {

    private final int number;

    private final Inventory inventory;

    public InventoryPage(int number) {
      this.number = number;
      this.inventory = Bukkit.createInventory(this, 6 * 9, Component.text("Stash"));
      Bukkit.getPluginManager().registerEvents(this, GameSurvival.getPlugin());
    }

    public void update(Map<Integer, MessiChestItem> itemBySlot) {
      this.inventory.clear();
      this.inventory.setItem(UP.getSlot(), UP);
      this.inventory.setItem(PAGE.getSlot(),
          PAGE.cloneWithId().setDisplayName("§9Page " + this.number));
      this.inventory.setItem(DOWN.getSlot(), DOWN);
      this.inventory.setItem(MessiChestVanillaView.this.orderItem.getSlot(),
          MessiChestVanillaView.this.orderItem);
      this.inventory.setItem(ID.getSlot(), ID.cloneWithId()
          .setLore("§f" + MessiChestVanillaView.this.chest.getId()));

      for (Map.Entry<Integer, MessiChestItem> entry : itemBySlot.entrySet()) {
        this.inventory.setItem(entry.getKey(), entry.getValue().getDisplayItem());
      }
    }

    public int getNumber() {
      return number;
    }

    @Override
    public @NotNull Inventory getInventory() {
      return this.inventory;
    }

    @EventHandler
    public void onUserInventoryClick(InventoryClickEvent event) {
      if (!this.inventory.equals(event.getClickedInventory())) {
        return;
      }

      ExItemStack clickedItem = ExItemStack.getItem(event.getCurrentItem(), false);
      Player player = (Player) event.getWhoClicked();

      if (event.getRawSlot() > 6 * 9) {
        return;
      }

      event.setCancelled(true);

      if (clickedItem == null) {
        return;
      }

      if (MessiChestVanillaView.this.tryHandleActionItem(player, clickedItem, this.number)) {
        return;
      }

      InventoryAction action = event.getAction();

      ItemStack itemOnCursor = player.getItemOnCursor();

      if (action.equals(InventoryAction.PLACE_ALL) || action.equals(InventoryAction.PLACE_SOME)) {
        MessiChestVanillaView.this.chest.onInteract(player,
            new MessiChest.AddAction(itemOnCursor, itemOnCursor.getAmount(), -1));
      } else if (action.equals(InventoryAction.PLACE_ONE)) {
        MessiChestVanillaView.this.chest.onInteract(player,
            new MessiChest.AddAction(itemOnCursor, 1, -1));
      } else {
        MessiChestItem messiChestItem = MessiChestVanillaView.this.chest.itemByDisplayItemOrHash.get1(clickedItem);

        if (action.equals(InventoryAction.SWAP_WITH_CURSOR)) {
          if (event.getClick().equals(ClickType.LEFT)) {
            MessiChestVanillaView.this.chest.onInteract(player,
                new MessiChest.AddAction(itemOnCursor, itemOnCursor.getAmount(), -1));
          } else if (event.getClick().equals(ClickType.RIGHT)) {
            MessiChestVanillaView.this.chest.onInteract(player,
                new MessiChest.AddAction(itemOnCursor, 1, -1));
          }
        } else if (messiChestItem != null) {
          if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            MessiChestVanillaView.this.chest.onInteract(player,
                new MessiChest.MoveToPlayerInventoryAction(clickedItem, messiChestItem.getItem().getMaxStackSize()));
          } else if (action.equals(InventoryAction.PICKUP_HALF)) {
            MessiChestVanillaView.this.chest.onInteract(player, new MessiChest.TakeToCursorAction(clickedItem, 1));
          } else if (action.equals(InventoryAction.PICKUP_ALL)) {
            MessiChestVanillaView.this.chest.onInteract(player, new MessiChest.TakeToCursorAction(clickedItem,
                messiChestItem.getItem().getMaxStackSize()));
          } else {
            return;
          }
        }
      }

      MessiChestVanillaView.this.updateItems();
      MessiChestVanillaView.this.chest.updateInventories();
    }

  }

}
