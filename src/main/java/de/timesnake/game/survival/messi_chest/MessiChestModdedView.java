/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.messi_chest;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.library.packets.util.listener.PacketHandler;
import de.timesnake.library.packets.util.listener.PacketPlayInListener;
import de.timesnake.library.packets.util.listener.PacketPlayOutListener;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MessiChestModdedView implements InventoryHolder, PacketPlayOutListener, PacketPlayInListener {

  private static final String NAME_PREFIX = "ts-messi-chest";
  private static final String DISPLAY_NAME = "Messi Chest";

  private static MenuType<?> menuType;

  public static void registerMenuType() {
    try {
      Field frozen = BuiltInRegistries.MENU.getClass().getDeclaredField("frozen");
      frozen.setAccessible(true);
      frozen.set(BuiltInRegistries.MENU, false);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    Object menuSupplier = Proxy.newProxyInstance(
        MenuType.class.getDeclaredClasses()[0].getClassLoader(),
        new Class[]{MenuType.class.getDeclaredClasses()[0]}, (proxy, method, args) -> {
          String method_name = method.getName();
          Class<?>[] classes = method.getParameterTypes();

          if (method_name.equals("create")) {
            if (classes.length == 2) {
              if (classes[0] == int.class && classes[1] == net.minecraft.world.entity.player.Inventory.class) {
                return new Object();
              }
            }
          }
          return null;
        });

    Method registerMethod;
    try {
      registerMethod = MenuType.class.getDeclaredMethod("register", String.class,
          MenuType.class.getDeclaredClasses()[0]);
      registerMethod.setAccessible(true);
      menuType = (MenuType<?>) registerMethod.invoke(null, "timesnake:stash", menuSupplier);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    BuiltInRegistries.MENU.freeze();
  }

  private final MessiChest chest;

  private final HashMap<Player, Integer> invIdByPlayer = new HashMap<>();
  private final HashMap<Player, Integer> revisionByPlayer = new HashMap<>();

  private final Inventory inventory;

  public MessiChestModdedView(MessiChest chest) {
    this.chest = chest;

    this.inventory = Bukkit.createInventory(this, 54, NAME_PREFIX + this.chest.getId());

    Server.getPacketManager().addListener((PacketPlayOutListener) this);
    Server.getPacketManager().addListener((PacketPlayInListener) this);
  }

  public void openInventoryFor(Player player) {
    player.openInventory(this.inventory);
    this.chest.currentViewers.add(player);
  }

  public void closeInventoryFor(Player player) {
    this.chest.currentViewers.remove(player);
    this.invIdByPlayer.remove(player);
    this.revisionByPlayer.remove(player);
  }

  private boolean hasPlayerStashScreenOpen(Player player, int containerId) {
    return this.invIdByPlayer.containsKey(player) && containerId == this.invIdByPlayer.get(player);
  }

  private ArrayList<net.minecraft.world.item.ItemStack> getItemsAsNms() {
    return new ArrayList<>(this.chest.itemByDisplayItemOrHash.getMap1().entrySet().stream()
        .map(e -> CraftItemStack.asNMSCopy(e.getKey().cloneWithId().asQuantity(e.getValue().getItem().getAmount())))
        .toList());
  }


  @PacketHandler(type = {
      ServerboundContainerClickPacket.class,
      ServerboundContainerClosePacket.class,
      ClientboundOpenScreenPacket.class,
      ClientboundContainerSetContentPacket.class,
      ClientboundContainerSetSlotPacket.class},
      modify = true)
  public Packet<?> onPacket(Packet<?> packet, Player player) {
    return switch (packet) {
      case ServerboundContainerClickPacket clickPacket -> {
        net.minecraft.world.inventory.ClickType clickType = clickPacket.getClickType();
        int buttonNum = clickPacket.getButtonNum();
        PacketKey packetKey = null;
        if (buttonNum == 0) {
          packetKey = PacketKey.LEFT;
        } else if (buttonNum == 1) {
          packetKey = PacketKey.RIGHT;
        }

        int slot = clickPacket.getSlotNum();
        ItemStack carriedItem = clickPacket.getCarriedItem().getBukkitStack();

        HashMap<Integer, ItemStack> itemBySlot = new HashMap<>(
            clickPacket.getChangedSlots().size());

        for (Int2ObjectMap.Entry<net.minecraft.world.item.ItemStack> entry : clickPacket.getChangedSlots()
            .int2ObjectEntrySet()) {
          itemBySlot.put(entry.getIntKey(), entry.getValue().getBukkitStack());
        }

        if (!this.hasPlayerStashScreenOpen(player, clickPacket.getContainerId())) {
          yield packet;
        }

        if (itemBySlot.keySet().stream().noneMatch(k -> k < 54) && !clickType.equals(
            net.minecraft.world.inventory.ClickType.QUICK_MOVE)) {
          yield packet;
        }

        boolean result = this.handleInteractPacket(player, clickType, packetKey, slot, carriedItem,
            itemBySlot);
        if (result) {
          this.chest.updateInventories();
        } else {
          player.updateInventory();
        }
        yield null;
      }
      case ServerboundContainerClosePacket closePacket -> {
        if (!this.hasPlayerStashScreenOpen(player, closePacket.getContainerId())) {
          yield packet;
        }
        this.closeInventoryFor(player);
        yield closePacket;
      }
      case ClientboundOpenScreenPacket openScreenPacket -> {
        if (openScreenPacket.getTitle().getString().equals(NAME_PREFIX + this.chest.getId())) {
          this.invIdByPlayer.put(player, openScreenPacket.getContainerId());
          yield new ClientboundOpenScreenPacket(this.invIdByPlayer.get(player), menuType,
              net.minecraft.network.chat.Component.literal(DISPLAY_NAME));
        }
        yield packet;
      }
      case ClientboundContainerSetSlotPacket setSlotPacket -> {
        if (!this.hasPlayerStashScreenOpen(player, setSlotPacket.getContainerId())) {
          yield packet;
        }

        int revision = this.revisionByPlayer.compute(player, (k, v) -> v == null ? 0 : v + 1);
        yield new ClientboundContainerSetSlotPacket(setSlotPacket.getContainerId(), revision,
            setSlotPacket.getSlot(), setSlotPacket.getItem());
      }
      case ClientboundContainerSetContentPacket contentPacket -> {
        if (!this.hasPlayerStashScreenOpen(player, contentPacket.getContainerId())) {
          yield packet;
        }

        int revision = this.revisionByPlayer.compute(player, (k, v) -> v == null ? 0 : v + 1);

        ArrayList<net.minecraft.world.item.ItemStack> items = this.getItemsAsNms();
        items.add(net.minecraft.world.item.ItemStack.EMPTY);
        items.addAll(contentPacket.getItems().subList(54, contentPacket.getItems().size()));

        yield new ClientboundContainerSetContentPacket(contentPacket.getContainerId(), revision,
            NonNullList.of(net.minecraft.world.item.ItemStack.EMPTY,
                items.toArray(net.minecraft.world.item.ItemStack[]::new)),
            contentPacket.getCarriedItem());
      }
      default -> packet;
    };
  }

  private boolean handleInteractPacket(Player player, net.minecraft.world.inventory.ClickType clickType,
                                       PacketKey key, int slot, ItemStack carriedItem,
                                       HashMap<Integer, ItemStack> itemBySlot) {
    boolean result = false;

    if (clickType.equals(net.minecraft.world.inventory.ClickType.QUICK_MOVE)) {
      if (slot < 54) { // stash to player
        Map.Entry<Integer, ItemStack> target = itemBySlot.entrySet().stream()
            .filter(e -> e.getKey() != slot).min(Comparator.comparingInt(Map.Entry::getKey)).orElse(null);

        if (target == null) {
          return false;
        }

        ExItemStack clickedItem = new ExItemStack(target.getValue());
        if (key.equals(PacketKey.LEFT)) {
          result = this.chest.onInteract(player,
              new MessiChest.MoveToPlayerInventoryAction(clickedItem, clickedItem.getMaxStackSize()));
        } else if (key.equals(PacketKey.RIGHT)) {
          result = this.chest.onInteract(player,
              new MessiChest.MoveToPlayerInventoryAction(clickedItem, 1));
        }
      } else { // player to stash

        int playerSlot;
        if (slot < 81) { // player inv
          playerSlot = slot - 54 + 9;
        } else { // player hot bar
          playerSlot = slot - 54 - 27;
        }
        ItemStack targetItem = player.getInventory().getItem(playerSlot);

        if (targetItem == null) {
          return false;
        }

        if (key.equals(PacketKey.LEFT)) {
          result = this.chest.onInteract(player,
              new MessiChest.AddAction(targetItem, targetItem.getAmount(), playerSlot));
        } else if (key.equals(PacketKey.RIGHT)) {
          result = this.chest.onInteract(player, new MessiChest.AddAction(targetItem, 1, playerSlot));
        }
      }

    } else if (itemBySlot.size() == 1) {  // cursor
      ExItemStack clickedItem;
      if (!carriedItem.isEmpty()) {
        clickedItem = new ExItemStack(carriedItem);
        if (key.equals(PacketKey.LEFT)) {
          result = this.chest.onInteract(player,
              new MessiChest.TakeToCursorAction(clickedItem, clickedItem.getMaxStackSize()));
        } else if (clickType.equals(net.minecraft.world.inventory.ClickType.PICKUP)) {
          if (!player.getItemOnCursor().isEmpty()) {
            result = this.chest.onInteract(player,
                new MessiChest.AddAction(player.getItemOnCursor(), player.getItemOnCursor().getAmount(), -1));
          } else {
            result = this.chest.onInteract(player, new MessiChest.TakeToCursorAction(clickedItem, 1));
          }
        }
      } else {
        if (!player.getItemOnCursor().isEmpty()) {
          result = this.chest.onInteract(player,
              new MessiChest.AddAction(player.getItemOnCursor(), player.getItemOnCursor().getAmount(), -1));
        }
      }
    }
    this.chest.updateInventories();
    return result;
  }

  @Override
  public @NotNull Inventory getInventory() {
    return this.inventory;
  }

  private enum PacketKey {
    LEFT, RIGHT;
  }
}
