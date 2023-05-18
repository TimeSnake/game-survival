/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class StashFile extends ExFile {

  private static final String BLOCK = "block";
  private static final String OWNER = "owner";
  private static final String MEMBERS = "members";
  private static final String ITEMS = "items";

  public StashFile(Integer id) {
    super("survival/stash", "stash_" + id);
  }

  public void saveStash(Block block, UUID owner, List<UUID> members, Collection<StashItem> items) {
    super.setBlock(BLOCK, block).save();
    super.set(OWNER, owner.toString()).save();

    super.setUuidList(MEMBERS, members);

    super.config.set(ITEMS, items.stream().map(StashItem::getItem).collect(Collectors.toList()));
    super.save();
  }

  public Block getStashBlock() throws WorldNotExistException {
    return super.getBlock(BLOCK);
  }

  public UUID getStashOwnerId() {
    return super.getUUID(OWNER);
  }

  public List<UUID> getStashMembers() {
    return super.getUUIDList(MEMBERS);
  }

  public List<ItemStack> getStashItems() {
    return (List<ItemStack>) super.config.getList(ITEMS, new ArrayList<>(0));
  }
}
