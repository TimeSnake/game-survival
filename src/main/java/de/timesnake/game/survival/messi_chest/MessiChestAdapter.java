package de.timesnake.game.survival.messi_chest;

import com.google.gson.*;
import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class MessiChestAdapter implements JsonSerializer<MessiChest>, JsonDeserializer<MessiChest> {

  @Override
  public JsonElement serialize(MessiChest src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("id", src.getId());
    jsonObject.addProperty("owner", src.getOwner().toString());

    JsonArray members = new JsonArray();
    for (UUID member : src.getMembers()) {
      members.add(member.toString());
    }
    jsonObject.add("members", members);

    JsonObject jsonBlock = new JsonObject();
    jsonBlock.addProperty("world", src.getBlock().getWorld().getName());
    jsonBlock.addProperty("x", src.getBlock().getX());
    jsonBlock.addProperty("y", src.getBlock().getY());
    jsonBlock.addProperty("z", src.getBlock().getZ());
    jsonObject.add("block", jsonBlock);

    JsonArray items = new JsonArray();
    for (MessiChestItem item : src.getItems()) {
      JsonObject itemJson = new JsonObject();
      JsonArray itemByteArray = new JsonArray();
      for (byte value : item.getItem().asOne().serializeAsBytes()) {
        itemByteArray.add(value);
      }
      itemJson.add("item", itemByteArray);
      itemJson.addProperty("amount", item.getItem().getAmount());
      items.add(itemJson);
    }
    jsonObject.add("items", items);

    return jsonObject;
  }

  @Override
  public MessiChest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject jsonObject = json.getAsJsonObject();

    int id = jsonObject.get("id").getAsInt();
    UUID owner = UUID.fromString(jsonObject.get("owner").getAsString());

    List<UUID> members = jsonObject.get("members").getAsJsonArray().asList().stream()
        .map(e -> UUID.fromString(e.getAsString()))
        .toList();

    JsonObject jsonBlock = jsonObject.get("block").getAsJsonObject();
    String worldName = jsonBlock.get("world").getAsString();
    World world = Bukkit.getWorld(worldName);

    if (world == null) {
      throw new WorldNotExistException(worldName);
    }

    Block block = world.getBlockAt(jsonBlock.get("x").getAsInt(), jsonBlock.get("y").getAsInt(),
        jsonBlock.get("z").getAsInt());

    List<MessiChestItem> items = jsonObject.get("items").getAsJsonArray().asList().stream()
        .map(e -> {
          JsonObject itemJson = e.getAsJsonObject();
          JsonArray byteArray = itemJson.get("item").getAsJsonArray();

          byte[] itemBytes = new byte[byteArray.size()];
          for (int i = 0; i < byteArray.size(); i++) {
            itemBytes[i] = byteArray.get(i).getAsByte();
          }
          return new MessiChestItem(ItemStack.deserializeBytes(itemBytes).asQuantity(itemJson.get("amount").getAsInt()));
        })
        .toList();

    return new MessiChest(id, owner, members, block, items);
  }
}
