/*
 * game-survival.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
