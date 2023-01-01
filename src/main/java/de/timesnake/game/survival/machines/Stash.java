/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.game.survival.main.GameSurvival;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

public class Stash extends Machine implements Listener {

    public static final ExItemStack ITEM = ExItemStack.getHashedIdItem(Material.LODESTONE,
                    "machine_stash")
            .setDisplayName("§6Stash");

    public static void loadRecipe() {
        ShapedRecipe stashRecipe = new ShapedRecipe(NamespacedKey.minecraft("stash"), Stash.ITEM);

        stashRecipe.shape("PSG", "ECA", "NHN");

        stashRecipe.setIngredient('N', Material.NETHERITE_BLOCK);
        stashRecipe.setIngredient('H', Material.HONEYCOMB);
        stashRecipe.setIngredient('C', Material.CONDUIT);
        stashRecipe.setIngredient('A', Material.AMETHYST_SHARD);
        stashRecipe.setIngredient('E', Material.ECHO_SHARD);
        stashRecipe.setIngredient('S', Material.SHULKER_SHELL);
        stashRecipe.setIngredient('P', Material.PEARLESCENT_FROGLIGHT);
        stashRecipe.setIngredient('G', Material.GOAT_HORN);

        Bukkit.getServer().addRecipe(stashRecipe);
    }

    private static Integer convertItemToKeyItem(ItemStack item) {
        return item.asOne().hashCode();
    }

    private static final ExItemStack UP = new ExItemStack(Material.DETECTOR_RAIL, "§9Up").setSlot(0)
            .immutable();
    private static final ExItemStack PAGE = new ExItemStack(Material.PAPER, "§9Page 0").setSlot(9);
    private static final ExItemStack DOWN = new ExItemStack(Material.DETECTOR_RAIL,
            "§9Down").setSlot(18).immutable();
    private static final ExItemStack ORDER = new ExItemStack(Material.SPYGLASS, "§9Order").setSlot(
            36);
    private static final ExItemStack SEARCH = new ExItemStack(Material.NAME_TAG,
            "§9Search").setLore("§7Coming soon").setSlot(45).immutable();
    private final UUID owner;
    private final List<UUID> members;
    private final Map<Integer, InventoryPage> pageByNumber = new HashMap<>();
    private final List<StashItem> stashItemsOrdered = new LinkedList<>();
    private final Map<ExItemStack, StashItem> stashItemByDisplayItem = new HashMap<>();
    private final Map<Integer, StashItem> stashItemByItemHash = new HashMap<>();
    private int maxPage = 0;

    private OrderType orderType = OrderType.NUMBER;
    private final ExItemStack orderItem = ORDER.cloneWithId()
            .setLore("§7" + this.orderType.getDisplayName());

    public Stash(Integer id, Block block, UUID owner, List<UUID> memberUuids) {
        super(id, block);
        this.owner = owner;
        this.members = memberUuids;
        this.pageByNumber.put(0, new InventoryPage(0));
        this.updateInventories();

        Server.registerListener(this, GameSurvival.getPlugin());
    }

    public synchronized void updateInventories() {

        Set<StashItem> removeItems = new HashSet<>();

        HashMap<Integer, StashItem> itemBySlot = new HashMap<>();
        InventoryPage currentPage = this.pageByNumber.get(0);

        this.stashItemsOrdered.sort(this.orderType.getComparator());

        int slot = 0;
        for (StashItem item : this.stashItemsOrdered) {
            if (slot >= 6 * 9) {
                currentPage.update(itemBySlot);
                itemBySlot = new HashMap<>();

                slot = 0;
                currentPage = this.pageByNumber.computeIfAbsent(currentPage.getNumber() + 1,
                        InventoryPage::new);
            }

            if (slot % 9 == 0) {
                slot += 2;
            }
            if (slot >= 6 * 9) {
                break;
            }

            if (item.isEmpty()) {
                removeItems.add(item);
                continue;
            }

            itemBySlot.put(slot, item);
            slot++;
        }

        currentPage.update(itemBySlot);

        for (StashItem item : removeItems) {
            this.stashItemsOrdered.remove(item);
            this.stashItemByItemHash.remove(convertItemToKeyItem(item.getItem()));
            this.stashItemByDisplayItem.remove(item.getDisplayItem());
        }

        this.maxPage = currentPage.getNumber();
    }

    private boolean tryHandleActionItem(User user, ExItemStack item, int number) {
        if (item.equals(UP)) {
            if (number > 0) {
                user.openInventory(this.pageByNumber.get(number - 1).getInventory());
                user.playSoundItemClicked();
            }
        } else if (item.equals(DOWN)) {
            if (number < this.maxPage) {
                user.openInventory(this.pageByNumber.get(number + 1).getInventory());
                user.playSoundItemClicked();
            }
        } else if (item.equals(ORDER)) {
            this.orderType = OrderType.values()[(this.orderType.ordinal() + 1)
                    % OrderType.values().length];
            this.orderItem.setLore("§7" + this.orderType.getDisplayName());
            this.updateInventories();
        } else if (item.equals(SEARCH)) {
            // TODO search
        } else {
            return false;
        }

        return true;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public Collection<StashItem> getItems() {
        return this.stashItemsOrdered;
    }

    public void openInventoryFor(User user) {
        user.openInventory(this.pageByNumber.get(0).getInventory());
    }

    public synchronized boolean addItem(ItemStack item) {
        StashItem stashItem = this.stashItemByItemHash.get(convertItemToKeyItem(item));

        if (stashItem != null && !stashItem.isEmpty()) {
            stashItem.add(item);
        } else {
            stashItem = new StashItem(item);
            this.stashItemsOrdered.add(stashItem);
            this.stashItemByDisplayItem.put(stashItem.getDisplayItem(), stashItem);
            this.stashItemByItemHash.put(convertItemToKeyItem(stashItem.getItem()), stashItem);
        }
        return true;
    }

    @Override
    public Type getType() {
        return Type.STASH;
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

        User user = Server.getUser(e.getWhoClicked().getUniqueId());
        ItemStack item = e.getCurrentItem();

        if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            e.setCancelled(true);
            user.getInventory().clear(e.getSlot());
            this.addItem(item);
            this.updateInventories();
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

    public enum OrderType {
        NUMBER("Number",
                Comparator.comparingInt((StashItem i) -> i.getItem().getAmount()).reversed()),
        NAME("Name", Comparator.comparing(i -> i.getItem().getType().name()));

        private final String displayName;
        private final Comparator<StashItem> comparator;

        OrderType(String displayName, Comparator<StashItem> comparator) {
            this.displayName = displayName;
            this.comparator = comparator;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Comparator<StashItem> getComparator() {
            return this.comparator;
        }
    }

    public class InventoryPage implements UserInventoryClickListener, InventoryHolder {

        private final int number;
        private final ExInventory inventory;

        public InventoryPage(int number) {
            this.number = number;
            this.inventory = new ExInventory(6 * 9, Component.text("Stash"), this);
            Server.getInventoryEventManager().addClickListener(this, this);
        }

        public void update(Map<Integer, StashItem> itemBySlot) {
            this.inventory.getInventory().clear();
            this.inventory.setItemStack(UP);
            this.inventory.setItemStack(PAGE.cloneWithId().setDisplayName("§9Page " + this.number));
            this.inventory.setItemStack(DOWN);
            this.inventory.setItemStack(Stash.this.orderItem);
            this.inventory.setItemStack(SEARCH);

            for (Map.Entry<Integer, StashItem> entry : itemBySlot.entrySet()) {
                this.inventory.setItemStack(entry.getKey(), entry.getValue().getDisplayItem());
            }

            this.inventory.getInventory().getViewers().forEach(e -> ((Player) e).updateInventory());
        }

        public int getNumber() {
            return number;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return this.inventory.getInventory();
        }

        @Override
        public void onUserInventoryClick(UserInventoryClickEvent event) {
            ExItemStack clickedItem = event.getClickedItem();
            User user = event.getUser();

            event.setCancelled(true);

            if (clickedItem == null) {
                return;
            }

            if (Stash.this.tryHandleActionItem(user, clickedItem, this.number)) {
                return;
            }

            InventoryAction action = event.getAction();

            ItemStack itemOnCursor = user.getItemOnCursor();

            if (action.equals(InventoryAction.PLACE_ALL)) {
                boolean result = Stash.this.addItem(itemOnCursor);
                if (result) {
                    user.setItemOnCursor(new ItemStack(Material.AIR));
                    user.playSoundItemClicked();
                }
            } else if (action.equals(InventoryAction.PLACE_ONE)) {
                boolean result = Stash.this.addItem(itemOnCursor.asOne());
                if (result) {
                    user.setItemOnCursor(itemOnCursor.asQuantity(itemOnCursor.getAmount() - 1));
                    user.playSoundItemClicked();
                }
            } else if (action.equals(InventoryAction.PLACE_SOME)) {
                boolean result = Stash.this.addItem(itemOnCursor);
                if (result) {
                    user.setItemOnCursor(new ItemStack(Material.AIR));
                    user.playSoundItemClicked();
                }
            } else {
                StashItem stashItem = Stash.this.stashItemByDisplayItem.get(clickedItem);

                if (action.equals(InventoryAction.SWAP_WITH_CURSOR)) {
                    if (stashItem.stackable(itemOnCursor)) {
                        if (event.getClickType().equals(ClickType.LEFT)) {
                            user.setItemOnCursor(
                                    itemOnCursor.asQuantity(itemOnCursor.getAmount() - 1));
                            Stash.this.addItem(itemOnCursor.asOne());
                            user.playSoundItemClicked();
                        } else if (event.getClickType().equals(ClickType.RIGHT)) {
                            if (itemOnCursor.getAmount() < itemOnCursor.getMaxStackSize()) {
                                user.setItemOnCursor(
                                        itemOnCursor.add(stashItem.remove(1).getAmount()));
                                user.playSoundItemClicked();
                            }
                        }
                    } else {
                        if (event.getClickType().equals(ClickType.LEFT)) {
                            boolean result = Stash.this.addItem(itemOnCursor);
                            if (result) {
                                user.setItemOnCursor(new ItemStack(Material.AIR));
                                user.playSoundItemClicked();
                            }
                        } else if (event.getClickType().equals(ClickType.RIGHT)) {
                            user.setItemOnCursor(
                                    itemOnCursor.asQuantity(itemOnCursor.getAmount() - 1));
                            Stash.this.addItem(itemOnCursor.asOne());
                            user.playSoundItemClicked();
                        }
                    }

                } else if (stashItem != null) {
                    if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                        user.addItem(stashItem.remove(64));
                        user.playSoundItemClicked();
                    } else if (action.equals(InventoryAction.PICKUP_HALF)) {
                        user.setItemOnCursor(stashItem.remove(1).clone());
                        user.playSoundItemClicked();
                    } else if (action.equals(InventoryAction.PICKUP_ALL)) {
                        user.setItemOnCursor(stashItem.remove(64).clone());
                        user.playSoundItemClicked();
                    } else {
                        return;
                    }
                }
            }

            Stash.this.updateInventories();
        }
    }
}
