package de.timesnake.game.survival.hep;

import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.library.extension.util.chat.Chat;

public class SurvivalHEP {

    public static String getMessageNoBlockSelected() {
        return Chat.getSenderPlugin(Plugin.PRIVATE_BLOCKS) + ChatColor.WARNING + "No block selected " + Chat.getMessageCode("H", 1902, Plugin.PRIVATE_BLOCKS);
    }

    public static String getMessageSelectedBlockIsPrivate() {
        return Chat.getSenderPlugin(Plugin.PRIVATE_BLOCKS) + ChatColor.WARNING + "Selected block is already private " + Chat.getMessageCode("H", 1900, Plugin.PRIVATE_BLOCKS);
    }

    public static String getMessageSelectedBlockIsNotPrivate() {
        return Chat.getSenderPlugin(Plugin.PRIVATE_BLOCKS) + ChatColor.WARNING + "Selected block is not private " + Chat.getMessageCode("H", 1901, Plugin.PRIVATE_BLOCKS);
    }

    public static String getMessageNotPrivateBlockOwner() {
        return Chat.getSenderPlugin(Plugin.PRIVATE_BLOCKS) + ChatColor.WARNING + "This is not your private-block " + Chat.getMessageCode("H", 1903, Plugin.PRIVATE_BLOCKS);
    }

    public static String getMessageBlockIsPrivate() {
        return Chat.getSenderPlugin(Plugin.PRIVATE_BLOCKS) + ChatColor.WARNING + "This block is private " + Chat.getMessageCode("H", 1905, Plugin.PRIVATE_BLOCKS);
    }

}
