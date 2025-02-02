/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.survival.rewards;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.survival.server.SurvivalServer;
import de.timesnake.library.chat.ExTextColor;
import net.kyori.adventure.text.Component;
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
      Server.broadcastMessage(SurvivalServer.PLUGIN, user.getChatNameComponent()
          .append(Component.text(" gained the reward: ", ExTextColor.PUBLIC))
          .append(Component.text(reward.getName(), ExTextColor.VALUE)));
      user.addItem(reward.getPrizes());
    }
  }
}
