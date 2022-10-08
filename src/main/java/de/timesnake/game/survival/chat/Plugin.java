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
