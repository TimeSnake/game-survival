package de.timesnake.game.survival.messi_chest;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.library.chat.Code;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;

import java.util.List;

public class MessiChestCmd implements CommandListener {

  private final Code perm = SurvivalServer.PLUGIN.createPermssionCode("survival.messi_chest");

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
    sender.isPlayerElseExit(true);
    sender.hasPermissionElseExit(this.perm);

    MessiChestManager chestManager = SurvivalServer.getMessiChestManager();

    args.isLengthHigherEqualsElseExit(1, true);

    User user = sender.getUser();

    if (args.get(0).equalsIgnoreCase("toggleView")) {
      chestManager.setPlayerModdedViewEnabled(user.getUniqueId(),
          !chestManager.hasPlayerModdedViewEnabled(user.getUniqueId()));
      sender.sendPluginTDMessage("§s" +
                                 (chestManager.hasPlayerModdedViewEnabled(user.getUniqueId()) ? "Enabled" : "Disabled")
                                 + " modded view");
      return;
    }

    int chestId = args.get(0).toIntOrExit(true);

    MessiChest chest = chestManager.getMessiChest(chestId);

    if (chest == null || !chest.getOwner().equals(user.getUniqueId())) {
      sender.sendPluginTDMessage("§wMessi chest not found");
      return;
    }

    args.isLengthHigherEqualsElseExit(2, true);

    Argument action = args.get(1);

    if (action.equalsIgnoreCase("member")) {
      args.isLengthHigherEqualsElseExit(4, true);

      Argument addOrRemove = args.get(2);

      User member = args.get(3).toUser();

      if (member == null) {
        sender.sendMessagePlayerNotExist(args.getString(3));
        return;
      }

      if (addOrRemove.equalsIgnoreCase("add")) {
        if (chest.addMember(member.getUniqueId())) {
          sender.sendPluginTDMessage("§sAdded member §v" + member.getTDChatName());
        } else {
          sender.sendPluginTDMessage("§wCould not add member " + member.getTDChatName());
        }
      } else if (addOrRemove.equalsIgnoreCase("remove")) {
        if (chest.removeMember(member.getUniqueId())) {
          sender.sendPluginTDMessage("§sRemoved member " + member.getTDChatName());
        } else {
          sender.sendPluginTDMessage("§wCould no remove member " + member.getTDChatName());
        }
      }
    }
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.perm)
        .addArgument(new Completion("toggleView"))
        .addArgument(new Completion((sender, cmd, args) -> {
          User user = sender.getUser();
          if (user == null) return List.of();
          return SurvivalServer.getMessiChestManager().getChestsOfPlayer(user.getUniqueId()).stream()
              .map(c -> String.valueOf(c.getId()))
              .toList();
        })
            .addArgument(new Completion("member")
                .addArgument(new Completion("add")
                    .addArgument(Completion.ofPlayerNames()))
                .addArgument(new Completion("remove")
                    .addArgument(new Completion((sender, cmd, args) -> {
                      User user = sender.getUser();
                      if (user == null) return List.of();
                      if (args.length() == 0 || !args.get(0).isInt(false)) return List.of();
                      return SurvivalServer.getMessiChestManager().getMessiChest(args.get(0).toInt()).getMembers().stream()
                          .map(uuid -> Server.getUser(uuid).getName())
                          .toList();
                    })))));
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }
}
