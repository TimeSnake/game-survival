package de.timesnake.game.survival.rewards;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.survival.chat.Plugin;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

public class RewardHandler implements Listener {

    @EventHandler
    public void onStatisticIncrementEvent(PlayerStatisticIncrementEvent e) {
        Statistic stat = e.getStatistic();
        for (StatisticReward reward : StatisticReward.values()) {
            if (!stat.equals(reward.getType())) {
                continue;
            }
            if (e.getNewValue() != reward.getGoal()) {
                continue;
            }

            User user = Server.getUser(e.getPlayer());
            Server.broadcastMessage(Plugin.REWARDS, ChatColor.PUBLIC + user.getChatName() + " gained the reward: " + ChatColor.VALUE + reward.getName());
            user.addItem(reward.getPrizes());
        }
    }
}
