/*
 * game-survival.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.survival.rewards;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.game.survival.chat.Plugin;
import de.timesnake.library.basic.util.chat.ExTextColor;
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
            Server.broadcastMessage(Plugin.REWARDS, user.getChatNameComponent()
                    .append(Component.text(" gained the reward: ", ExTextColor.PUBLIC))
                    .append(Component.text(reward.getName(), ExTextColor.VALUE)));
            user.addItem(reward.getPrizes());
        }
    }
}
