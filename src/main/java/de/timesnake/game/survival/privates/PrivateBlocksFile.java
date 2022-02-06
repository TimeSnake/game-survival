package de.timesnake.game.survival.privates;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.game.survival.server.SurvivalServer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class PrivateBlocksFile extends ExFile {

    public static final String BLOCKS_PATH = "blocks";

    private static final String OWNER = "owner";
    private static final String PASSWORD = "password";
    private static final String IS_PUBLIC = "public";
    private static final String MEMBERS = "members";

    public PrivateBlocksFile() {
        super("survival", "privateBlocks");
    }

    public PrivateBlock getPrivateBlock(Integer id) {
        String path = BLOCKS_PATH + "." + id;
        if (super.contains(path)) {
            Location loc;
            try {
                loc = super.getLocation(path);
            } catch (WorldNotExistException e) {
                return null;
            }

            if (SurvivalServer.getPrivateBlockManger().hasInventory(loc.getBlock())) {
                return new PrivateInventoryBlock(id, loc.getBlock(), super.getUUID(path + "." + OWNER), super.getString(path + "." + PASSWORD), super.getBoolean(path + "+" + IS_PUBLIC), super.getUUIDList(path + "." + MEMBERS));
            } else {
                return new PrivateBlock(id, loc.getBlock(), super.getUUID(path + "." + OWNER), super.getString(path + "." + PASSWORD), super.getBoolean(path + "+" + IS_PUBLIC), super.getUUIDList(path + "." + MEMBERS));
            }
        }
        return null;
    }

    public void addPrivateBlock(PrivateBlock block) {
        Integer id = block.getId();
        String path = BLOCKS_PATH + "." + id;
        super.set(path, block.getBlock());
        super.set(path + id + "." + OWNER, block.getOwner());
        if (block.getPassword() != null) {
            super.set(path + "." + PASSWORD, block.getPassword());
        }
        super.set(path + "." + IS_PUBLIC, block.isPublic());
        if (block.getMembers().size() > 0) {
            String[] uuids = new String[block.getMembers().size()];
            for (int i = 0; i < block.getMembers().size(); i++) {
                uuids[i] = block.getMembers().get(i).toString();
            }
            super.set(path + "." + MEMBERS, uuids);
        }
    }

    public Collection<Integer> getIds() {
        Collection<Integer> ids = new ArrayList<>();

        Set<String> idStrings = super.getPathStringList(BLOCKS_PATH);
        if (idStrings != null) {
            for (String s : idStrings) {
                try {
                    ids.add(Integer.valueOf(s));
                } catch (NumberFormatException e) {
                    Server.printWarning(Plugin.SURVIVAL, "Can not get id from " + s, "Private");
                }
            }
        }
        return ids;
    }

    public void removePrivateBlock(Integer id) {
        super.remove(BLOCKS_PATH + "." + id);
    }

    public void resetPrivateBlocks() {
        super.remove(BLOCKS_PATH);
    }

}
