package de.timesnake.game.survival.chat;


public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

    public static final Plugin SURVIVAL = new Plugin("Survival", "GSS");
    public static final Plugin PRIVATE_BLOCKS = new Plugin("PrivateBlocks", "GSP");
    public static final Plugin MACHINES = new Plugin("Machine", "GSM");
    public static final Plugin REWARDS = new Plugin("Rewards", "GSR");

    protected Plugin(String name, String code) {
        super(name, code);
    }

}
