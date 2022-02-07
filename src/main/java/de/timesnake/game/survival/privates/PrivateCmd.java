package de.timesnake.game.survival.privates;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.game.survival.hep.SurvivalHEP;
import de.timesnake.game.survival.main.GameSurvival;
import de.timesnake.game.survival.player.SurvivalUser;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;

public class PrivateCmd implements CommandListener, Listener {

    private static HashMap<User, BukkitTask> waitForSelection = new HashMap<>();


    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!(cmd.getName().equalsIgnoreCase("private") || cmd.getName().equalsIgnoreCase("pv"))) {
            return;
        }
        if (!args.isLengthHigher(0, true)) {
            return;
        }
        if (!sender.isPlayer(true)) {
            return;
        }
        PrivateBlockManger manager = SurvivalServer.getPrivateBlockManger();
        SurvivalUser user = (SurvivalUser) Server.getUser(sender.getPlayer());
        PrivateBlock block;

        switch (args.get(0).toLowerCase()) {
            case "select":
                user.resetSelectedBlock();
                PrivateCmd.selectBlock(sender);
                break;

            case "create":
                if (user.hasBlockSelected()) {
                    block = manager.getBlock(user.getSelectedBlock());
                    if (block == null) {
                        if (sender.hasPermission("survival.privateblocks.create", 1801)) {
                            manager.addBlock(user.getSelectedBlock(), user);
                            user.resetSelectedBlock();
                        }
                    } else sender.sendMessage(SurvivalHEP.getMessageSelectedBlockIsPrivate());
                } else sender.sendMessage(SurvivalHEP.getMessageNoBlockSelected());
                break;

            case "remove":
                if (!user.hasBlockSelected()) {
                    sender.sendMessage(SurvivalHEP.getMessageNoBlockSelected());
                    return;
                }
                block = manager.getBlock(user.getSelectedBlock());
                if (block == null) {
                    sender.sendMessage(SurvivalHEP.getMessageSelectedBlockIsNotPrivate());
                    return;
                }
                if (!(block.getOwner().equals(user.getUniqueId()) || sender.hasPermission("survival.privateblocks.remove.other"))) {
                    sender.sendMessage(SurvivalHEP.getMessageNotPrivateBlockOwner());
                    return;
                }
                if (!(sender.hasPermission("survival.privateblocks.remove") || sender.hasPermission("survival.privateblocks.remove.other"))) {
                    sender.sendMessageNoPermission(1802);
                    return;
                }

                manager.removeBlock(block, user);
                user.resetSelectedBlock();
                break;

            case "member":
                if (user.hasBlockSelected()) {
                    sender.sendMessage(SurvivalHEP.getMessageNoBlockSelected());
                    return;
                }
                block = manager.getBlock(user.getSelectedBlock());
                if (block == null) {
                    sender.sendMessage(SurvivalHEP.getMessageSelectedBlockIsNotPrivate());
                    return;
                }
                if (!(block.getOwner().equals(user.getUniqueId()) || sender.hasPermission("survival.privateblocks.member.add.other") || sender.hasPermission("survival.privateblocks.member.remove.other"))) {
                    sender.sendMessage(SurvivalHEP.getMessageNotPrivateBlockOwner());
                    return;
                }
                if (!args.isLengthHigher(1, true)) {
                    return;
                }


                if (args.get(1).equalsIgnoreCase("add")) {
                    if (!(sender.hasPermission("survival.privateblocks.member.add") || sender.hasPermission("survival.privateblocks.member.add.other"))) {
                        sender.sendMessageNoPermission(1800);
                    }
                    if (!args.isLengthEquals(3, true) || !args.get(2).isPlayerDatabaseName(true)) {
                        return;
                    }

                    block.addMember(args.get(2).toDbUser());
                    user.resetSelectedBlock();
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Added member " + ChatColor.VALUE + args.get(2).toDbUser().getName());
                } else if (args.get(1).equalsIgnoreCase("remove")) {
                    if (!(sender.hasPermission("survival.privateblocks.member.remove") || sender.hasPermission("survival.privateblocks.member.remove.other"))) {
                        sender.sendMessageNoPermission(1804);
                        return;
                    }
                    if (!args.isLengthEquals(3, true)) {
                        sender.sendMessageTooFewArguments();
                        return;
                    }
                    if (!args.get(2).isPlayerDatabaseName(true)) {
                        return;
                    }
                    if (block.getMembers().contains(args.get(2).toDbUser().getUniqueId())) {
                        block.removeMember(args.get(2).toDbUser());
                        user.resetSelectedBlock();
                        sender.sendPluginMessage(ChatColor.PERSONAL + "Removed member " + ChatColor.VALUE + args.get(2).toDbUser().getName());
                    } else {
                        sender.sendPluginMessage(ChatColor.WARNING + "Member " + ChatColor.VALUE + args.get(2).toDbUser().getName() + ChatColor.WARNING + " is not a member " + Chat.getMessageCode("H", 1904, Plugin.PRIVATE_BLOCKS));
                    }
                } else {
                    sender.sendMessageTooFewArguments();
                    sender.sendMessageCommandHelp("Add/Remove member to privateblock", "private member add <player>");
                }
                break;

            case "password":
                if (!user.hasBlockSelected()) {
                    sender.sendMessage(SurvivalHEP.getMessageNoBlockSelected());
                    return;
                }
                block = manager.getBlock(user.getSelectedBlock());

                if (block == null) {
                    sender.sendMessage(SurvivalHEP.getMessageSelectedBlockIsNotPrivate());
                    return;
                }
                if (!(block.getOwner().equals(user.getUniqueId()) || sender.hasPermission("survival.privateblocks.password.other"))) {
                    sender.sendMessage(SurvivalHEP.getMessageNotPrivateBlockOwner());
                    return;
                }
                if (!(sender.hasPermission("survival.privateblocks.password") || sender.hasPermission("survival.privateblocks.password.other"))) {
                    return;
                }


                if (args.isLengthHigher(1, true)) {
                    block.setPassword(args.get(1).getString());
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Updated password to " + ChatColor.VALUE + args.get(1).getString());
                } else {
                    block.resetPassword();
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Reset password");
                }
                break;

            case "public":
                if (!user.hasBlockSelected()) {
                    sender.sendMessage(SurvivalHEP.getMessageNoBlockSelected());
                    return;
                }
                block = manager.getBlock(user.getSelectedBlock());


                if (block == null) {
                    sender.sendMessage(SurvivalHEP.getMessageSelectedBlockIsNotPrivate());
                    return;
                }
                if (!(block.getOwner().equals(user.getUniqueId()) || sender.hasPermission("survival.privateblocks.public.other", 1808))) {
                    sender.sendMessage(SurvivalHEP.getMessageNotPrivateBlockOwner());
                    return;
                }
                if (!(sender.hasPermission("survival.privateblocks.public") || sender.hasPermission("survival.privateblocks.public.other"))) {
                    sender.sendMessageNoPermission(1807);
                    return;
                }


                if (block.isPublic()) {
                    block.setIsPublic(false);
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Updated access to " + ChatColor.VALUE + "private");
                } else {
                    block.setIsPublic(true);
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Updated access to " + ChatColor.VALUE + "public");
                }
                break;
            default:
        }
    }

    public static void selectBlock(Sender sender) {
        sender.sendPluginMessage(ChatColor.PERSONAL + "Select a block by clicking");
        PrivateCmd.waitForSelection.put(sender.getUser(), Bukkit.getScheduler().runTaskLaterAsynchronously(GameSurvival.getPlugin(), () -> {
            sender.sendPluginMessage(ChatColor.PERSONAL + "Time is out");
            PrivateCmd.waitForSelection.remove(sender.getUser());
        }, 200));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (PrivateCmd.waitForSelection.containsKey(Server.getUser(e.getPlayer()))) {
                e.setCancelled(true);
                PrivateCmd.waitForSelection.get(Server.getUser(e.getPlayer())).cancel();
                SurvivalUser user = (SurvivalUser) Server.getUser(e.getPlayer());
                Block block = e.getClickedBlock();
                PrivateCmd.waitForSelection.remove(Server.getUser(e.getPlayer()));
                user.setSelectedBlock(block);
                user.sendPluginMessage(Plugin.PRIVATE_BLOCKS, ChatColor.PERSONAL + "Block selected");
            }
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return null;
    }
}
