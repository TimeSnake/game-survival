package de.timesnake.game.survival.messi_chest;

import com.google.gson.reflect.TypeToken;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserBlockBreakEvent;
import de.timesnake.basic.bukkit.util.user.event.UserBlockPlaceEvent;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExBlock;
import de.timesnake.game.survival.main.GameSurvival;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.library.basic.util.GsonFile;
import de.timesnake.library.basic.util.MultiKeyMap;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MessiChestManager implements Listener {

  public static void loadRecipe() {
    ShapedRecipe stashRecipe = new ShapedRecipe(NamespacedKey.minecraft("messi_chest"), MessiChest.ITEM);

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


  private final HashSet<Integer> usedIds = new HashSet<>();
  private final MultiKeyMap<Integer, Block, MessiChest> chests = new MultiKeyMap<>();

  private final HashMap<UUID, Boolean> moddedViewEnabledByUuid = new HashMap<>();

  private final Path stashFolderPath = Path.of("plugins", "survival", "chests");

  private final Logger logger;

  public MessiChestManager() {
    this.logger = LogManager.getLogger("messi-chest.manager");

    loadRecipe();
    MessiChestModdedView.registerMenuType();
    Configurator.initialize(null, "plugins/survival/log4j2.xml");

    Bukkit.getPluginManager().registerEvents(this, GameSurvival.getPlugin());
  }

  public void load() {
    this.loadMessiChests();
    this.loadPlayerModdedViews();
  }

  private void loadMessiChests() {
    String[] fileNames = stashFolderPath.toFile().list((dir, name) -> name.startsWith("chest") && name.endsWith(
        ".json"));
    ArrayList<String> ids = new ArrayList<>();

    if (fileNames != null) {
      for (String fileName : fileNames) {
        MessiChest chest = MessiChest.createFromJsonFile(stashFolderPath.resolve(fileName).toFile());
        this.addMessiChest(chest);
        ids.add(String.valueOf(chest.getId()));
      }
    }

    this.logger.info("Loaded messi-chests {}", String.join(", ", ids));
  }

  private void loadPlayerModdedViews() {
    File file = new File("plugins/survival/player_modded_view.json");

    if (!file.exists()) {
      return;
    }


    Object list = new GsonFile(file).read(new TypeToken<ArrayList<UUID>>() {
    }.getType());

    if (list != null) {
      this.moddedViewEnabledByUuid.putAll(((ArrayList<UUID>) list).stream().collect(Collectors.toMap(e -> e,
          e -> true)));
    }
  }

  public void save() {
    this.saveMessiChests();
    this.savePlayerModdedViews();
  }

  private void saveMessiChests() {
    ArrayList<String> ids = new ArrayList<>();
    for (MessiChest chest : this.chests.values()) {
      chest.saveToFile(this.stashFolderPath);
      ids.add(String.valueOf(chest.getId()));
    }

    this.logger.info("Saved messi-chests {}", String.join(", ", ids));
  }

  private void savePlayerModdedViews() {
    File file = new File("plugins/survival/player_modded_view.json");
    try {
      FileUtils.createParentDirectories(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    new GsonFile(file).write(this.moddedViewEnabledByUuid.entrySet().stream()
        .filter(Map.Entry::getValue)
        .map(Map.Entry::getKey)
        .toList());
  }

  public MessiChest createMessiChest(UUID owner, Block block) {
    MessiChest chest = new MessiChest(this.newId(), owner, List.of(), block, List.of());
    this.addMessiChest(chest);
    this.logger.info("Created messi-chest {}", chest.getId());
    return chest;
  }

  public void addMessiChest(MessiChest chest) {
    this.chests.put(chest.getId(), chest.getBlock(), chest);
    this.usedIds.add(chest.getId());
  }

  public void removeMessiChest(@Nullable Player player, @NotNull MessiChest chest) {
    this.chests.remove(chest.getId(), chest.getBlock());
    this.usedIds.remove(chest.getId());


    if (chest.deleteFile(this.stashFolderPath)) {
      this.logger.info("Deleted messi-chest {} and file", chest.getId());
    } else {
      this.logger.info("Deleted messi-chest {}", chest.getId());
    }

    if (player != null) {
      player.sendMessage("Deleted Messi Chest (" + chest.getId() + ")");
    }
  }

  public MessiChest getMessiChest(int id) {
    return this.chests.get1(id);
  }

  public Path getStashFolderPath() {
    return stashFolderPath;
  }

  public Boolean hasPlayerModdedViewEnabled(UUID uuid) {
    return this.moddedViewEnabledByUuid.getOrDefault(uuid, false);
  }

  public void setPlayerModdedViewEnabled(UUID uuid, boolean enabled) {
    this.moddedViewEnabledByUuid.put(uuid, enabled);
  }

  private int newId() {
    int id = usedIds.size();
    while (usedIds.contains(id)) {
      id++;
    }
    return id;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e) {
    Block block = e.getClickedBlock();

    if (block == null) {
      return;
    }

    MessiChest chest = this.chests.get2(block);

    if (chest == null) {
      return;
    }

    User user = Server.getUser(e.getPlayer());

    if (!user.isSneaking() && e.getAction().isRightClick()) {
      e.setCancelled(true);
      if (!chest.openInventoryFor(user.getPlayer())) {
        user.sendPluginTDMessage(SurvivalServer.PLUGIN, "§wAccess denied");
      }
    }
  }

  @EventHandler
  public void onBlockPlace(UserBlockPlaceEvent e) {
    Block block = e.getBlock();
    User user = e.getUser();
    ExItemStack item = ExItemStack.getItem(e.getItemInHand(), true);

    if (MessiChest.ITEM.equals(item)) {
      MessiChest chest = this.createMessiChest(user.getUniqueId(), block);
      user.sendPluginTDMessage(SurvivalServer.PLUGIN, "§sMessi chest created (id: §v" + chest.getId() + ")");
      return;
    }

    Block blockPlaced = e.getBlockPlaced();

    if (!(blockPlaced.getState() instanceof Container container)) {
      return;
    }

    Optional<MessiChest> chestOpt = new ExBlock(blockPlaced).getBesideBlocks().stream()
        .map(this.chests::get2)
        .filter(Objects::nonNull)
        .findFirst();

    if (chestOpt.isPresent()) {
      MessiChest chest = chestOpt.get();
      Inventory blockInv = container.getInventory();
      chest.addItem(user.getPlayer(), blockInv.getContents());
      chest.updateInventories();
      blockInv.clear();
      blockPlaced.getWorld().playNote(blockPlaced.getLocation(), Instrument.STICKS, Note.natural(1, Note.Tone.A));
    }
  }

  @EventHandler
  public void onBlockBreak(UserBlockBreakEvent e) {
    Block block = e.getBlock();
    User user = e.getUser();

    MessiChest chest = this.chests.get2(block);
    if (chest != null) {
      if (chest.getItems().isEmpty()) {
        if (chest.getOwner().equals(user.getUniqueId())) {
          this.removeMessiChest(user.getPlayer(), chest);
          e.setDropItems(false);
          user.sendPluginTDMessage(SurvivalServer.PLUGIN, "§sDestroyed messi chest (id: §v" + chest.getId() + ")");
        } else {
          e.setDropItems(false);
          e.setCancelled(true);
          user.sendPluginTDMessage(SurvivalServer.PLUGIN, "§wThis is not your messi chest");
        }
      } else {
        e.setDropItems(false);
        e.setCancelled(true);
        user.sendPluginTDMessage(SurvivalServer.PLUGIN, "§wStash must be empty");
      }
    }
  }

  public Collection<MessiChest> getChestsOfPlayer(UUID uuid) {
    return this.chests.values().stream()
        .filter(c -> c.getOwner() == uuid)
        .toList();
  }
}
