package de.timesnake.game.survival.machines;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

public class StashCmd implements CommandListener {

    private Code.Permission perm;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.hasPermission(this.perm)) {
            return;
        }

        if (!sender.isPlayer(true)) {
            return;
        }

        User user = sender.getUser();

        Optional<Machine> stashOptional = SurvivalServer.getMachineManager().getMachineByBlockByType().get(Machine.Type.STASH)
                .values().stream().filter(s -> ((Stash) s).getOwner().equals(user.getUniqueId())).findFirst();

        if (stashOptional.isEmpty()) {
            sender.sendPluginMessage(Component.text("No stash found ", ExTextColor.WARNING)
                    .append(Chat.getMessageCode("H", 1908, Plugin.SURVIVAL).color(ExTextColor.WARNING)));
            return;
        }

        Stash stash = ((Stash) stashOptional.get());

        if (!args.isLengthHigherEquals(2, true)) {
            return;
        }

        if (!args.get(1).isPlayerName(true)) {
            return;
        }

        User member = args.get(1).toUser();

        switch (args.getString(0).toLowerCase()) {
            case "addmember" -> {
                if (stash.getMembers().contains(member.getUniqueId())) {
                    sender.sendPluginMessage(Component.text("Player ", ExTextColor.WARNING)
                            .append(member.getChatNameComponent())
                            .append(Component.text(" is already a stash member", ExTextColor.WARNING)));
                    return;
                }

                stash.getMembers().add(member.getUniqueId());
                sender.sendPluginMessage(Component.text("Added player ", ExTextColor.PERSONAL)
                        .append(member.getChatNameComponent())
                        .append(Component.text(" as member to the stash", ExTextColor.PERSONAL)));
            }
            case "removemember" -> {
                if (!stash.getMembers().contains(member.getUniqueId())) {
                    sender.sendPluginMessage(Component.text("Player ", ExTextColor.WARNING)
                            .append(member.getChatNameComponent())
                            .append(Component.text(" is not a stash member", ExTextColor.WARNING)));
                    return;
                }

                stash.getMembers().remove(member.getUniqueId());
                sender.sendPluginMessage(Component.text("Removed player ", ExTextColor.PERSONAL)
                        .append(member.getChatNameComponent())
                        .append(Component.text(" from the stash", ExTextColor.PERSONAL)));
            }
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("addmember", "removemember");
        } else if (args.length() == 2) {
            return Server.getCommandManager().getTabCompleter().getPlayerNames();
        }
        return List.of();
    }

    @Override
    public void loadCodes(de.timesnake.library.extension.util.chat.Plugin plugin) {
        this.perm = plugin.createPermssionCode("sur", "survival.stash");
    }
}
