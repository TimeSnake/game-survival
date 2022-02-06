package de.timesnake.game.survival.privates;

import de.timesnake.database.util.user.DbUser;
import org.bukkit.block.Block;

import java.util.List;
import java.util.UUID;


public class PrivateBlock {

    private final Integer id;
    private final Block block;
    private final UUID owner;
    private boolean isPublic;
    private String password;
    private List<UUID> members;

    public PrivateBlock(Integer id, Block block, UUID owner, String password, boolean isPublic, List<UUID> members) {
        this.id = id;
        this.block = block;
        this.owner = owner;
        this.isPublic = isPublic;
        this.password = password;
        this.members = members;
    }

    public Integer getId() {
        return id;
    }

    public Block getBlock() {
        return this.block;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void resetPassword() {
        this.password = null;
    }

    public void addMember(DbUser dbUser) {
        this.isPublic = false;
        this.members.add(dbUser.getUniqueId());
    }

    public void removeMember(DbUser dbUser) {
        this.members.remove(dbUser.getUniqueId());
    }

    public void setIsPublic(boolean isPublic) {
        if (isPublic) {
            this.members.clear();
        }
        this.isPublic = isPublic;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

}
