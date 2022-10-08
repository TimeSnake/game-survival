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

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.library.basic.util.Tuple;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public enum StatisticReward implements Reward {

    BLOCK_BREAK("Miner", Statistic.MINE_BLOCK, 100000, new ExItemStack(Material.DIAMOND_PICKAXE).setDamage(1)),
    LONG_DISTANCE_RUNNER("Long-Distance-Runner", Statistic.SPRINT_ONE_CM, 10000000,
            new ExItemStack(Material.DIAMOND_BOOTS).unbreakable()),
    KNIGHT("Knight", Statistic.HORSE_ONE_CM, 100000, new ExItemStack(Material.GOLDEN_HORSE_ARMOR),
            new ExItemStack(Material.IRON_SWORD)),
    TRAIN_DRIVER("Train-Driver", Statistic.MINECART_ONE_CM, 10000, new ExItemStack(Material.POWERED_RAIL, 64)),
    PILOT("Pilot", Statistic.FLY_ONE_CM, 100000, new ExItemStack(Material.ELYTRA)),
    BASE_JUMPING("Base-Jumping", Statistic.FALL_ONE_CM, 10000,
            new ExItemStack(Material.ENCHANTED_BOOK).addEnchantments(new Tuple<>(Enchantment.PROTECTION_FALL, 5))),
    FARMER("Farmer", Statistic.ANIMALS_BRED, 100, new ExItemStack(Material.SADDLE),
            new ExItemStack(Material.WOODEN_HOE).unbreakable()),
    DOORBELL_PRANK("Doorbell-Prank", Statistic.BELL_RING, 11, ExItemStack.getPotion(ExItemStack.PotionMaterial.SPLASH
            , 1, "§fPrankster", PotionEffectType.INVISIBILITY, 20 * 60 * 8, 1)),
    BIRTHDAY("Birthday", Statistic.CAKE_SLICES_EATEN, 7, new ExItemStack(Material.CAKE),
            new ExItemStack(Material.PUMPKIN_PIE, 32)),
    FISHER("Fisher", Statistic.FISH_CAUGHT, 64, new ExItemStack(Material.FISHING_ROD).unbreakable()),
    ADDICTED("Addicted", Statistic.PLAY_ONE_MINUTE, 60 * 24, new ExItemStack(Material.CLOCK)),
    GARDENER("Gardener", Statistic.FLOWER_POTTED, 43, new ExItemStack(Material.FLOWER_POT, 16),
            new ExItemStack(Material.OAK_SIGN, 16)),
    HERO("Hero", Statistic.RAID_WIN, 9, new ExItemStack(Material.TOTEM_OF_UNDYING, 9)),
    SCOUT("Scout", Statistic.INTERACT_WITH_CAMPFIRE, 4, new ExItemStack(Material.LEATHER_HELMET).unbreakable()),
    MUSICIAN("Musician", Statistic.NOTEBLOCK_PLAYED, 64, new ExItemStack(Material.JUKEBOX)),
    CAPTAIN("Captain", Statistic.BOAT_ONE_CM, 100000,
            new ExItemStack(Material.DIAMOND_BOOTS).addEnchantments(new Tuple<>(Enchantment.DEPTH_STRIDER, 4))),


    ;
    private final String name;
    private final Statistic type;
    private final int goal;
    private final ExItemStack[] prizes;

    StatisticReward(String name, Statistic type, int goal, ExItemStack... prizes) {
        this.name = name;
        this.type = type;
        this.goal = goal;
        this.prizes = prizes;
    }

    @Override
    public String getName() {
        return name;
    }

    public Statistic getType() {
        return type;
    }

    @Override
    public int getGoal() {
        return goal;
    }

    @Override
    public ExItemStack[] getPrizes() {
        return prizes;
    }
}
