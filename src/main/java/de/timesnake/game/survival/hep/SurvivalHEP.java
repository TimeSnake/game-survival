package de.timesnake.game.survival.hep;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.game.survival.chat.Plugin;

public class SurvivalHEP {

    public static String getMessageNoBlockSelected() {
        return Server.getChat().getSenderPlugin(Plugin.PRIVATE_BLOCKS) + ChatColor.WARNING + "No block selected " + Server.getChat().getMessageCode("H", 1902, Plugin.PRIVATE_BLOCKS);
    }

    public static String getMessageSelectedBlockIsPrivate() {
        return Server.getChat().getSenderPlugin(Plugin.PRIVATE_BLOCKS) + ChatColor.WARNING + "Selected block is already private " + Server.getChat().getMessageCode("H", 1900, Plugin.PRIVATE_BLOCKS);
    }

    public static String getMessageSelectedBlockIsNotPrivate() {
        return Server.getChat().getSenderPlugin(Plugin.PRIVATE_BLOCKS) + ChatColor.WARNING + "Selected block is not private " + Server.getChat().getMessageCode("H", 1901, Plugin.PRIVATE_BLOCKS);
    }

    public static String getMessageNotPrivateBlockOwner() {
        return Server.getChat().getSenderPlugin(Plugin.PRIVATE_BLOCKS) + ChatColor.WARNING + "This is not your private-block " + Server.getChat().getMessageCode("H", 1903, Plugin.PRIVATE_BLOCKS);
    }

    public static String getMessageBlockIsPrivate() {
        return Server.getChat().getSenderPlugin(Plugin.PRIVATE_BLOCKS) + ChatColor.WARNING + "This block is private " + Server.getChat().getMessageCode("H", 1905, Plugin.PRIVATE_BLOCKS);
    }

}
