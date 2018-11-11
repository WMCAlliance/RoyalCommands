/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdIngot2Block extends TabCommand {

    public CmdIngot2Block(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    public void i2b(Player p, ItemStack hand, Material ingot, Material block) {
        i2b(p, hand, ingot, block, hand.getDurability());
    }

    public void i2b(Player p, ItemStack hand, Material ingot, Material block, short data) {
        int remainder = hand.getAmount() % 9;
        int amount = hand.getAmount() - remainder;
        int blocks = amount / 9;
        if (blocks > 0) {
            final ItemStack blocka = new ItemStack(block, blocks);
            final ItemStack ingots = new ItemStack(ingot, amount, data);
            p.getInventory().removeItem(ingots);
            final HashMap<Integer, ItemStack> left = p.getInventory().addItem(blocka);
            if (!left.isEmpty()) for (ItemStack s : left.values()) p.getWorld().dropItemNaturally(p.getLocation(), s);
        }
        p.sendMessage(MessageColor.POSITIVE + "Made " + MessageColor.NEUTRAL + blocks + " block(s) " + MessageColor.POSITIVE + "and had " + MessageColor.NEUTRAL + remainder + " material(s) " + MessageColor.POSITIVE + "left over.");
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            cs.sendMessage(MessageColor.NEGATIVE + "Your hand is empty!");
            return true;
        }
        switch (hand.getType()) {
            case IRON_INGOT:
                i2b(p, hand, Material.IRON_INGOT, Material.IRON_BLOCK);
                break;
            case GOLD_INGOT:
                i2b(p, hand, Material.GOLD_INGOT, Material.GOLD_BLOCK);
                break;
            case DIAMOND:
                i2b(p, hand, Material.DIAMOND, Material.DIAMOND_BLOCK);
                break;
            case GOLD_NUGGET:
                i2b(p, hand, Material.GOLD_NUGGET, Material.GOLD_INGOT);
                break;
            case EMERALD:
                i2b(p, hand, Material.EMERALD, Material.EMERALD_BLOCK);
                break;
            case COAL:
                i2b(p, hand, Material.COAL, Material.COAL_BLOCK);
                break;
            case QUARTZ:
                i2b(p, hand, Material.QUARTZ, Material.QUARTZ_BLOCK);
                break;
            case REDSTONE:
                i2b(p, hand, Material.REDSTONE, Material.REDSTONE_BLOCK);
                break;
            case INK_SAC:
                if (hand.getDurability() == 4) {
                    i2b(p, hand, Material.INK_SAC, Material.LAPIS_BLOCK, (short) 4);
                    break;
                }
            case WHEAT:
                i2b(p, hand, Material.WHEAT, Material.HAY_BLOCK);
                break;
            default:
                cs.sendMessage(MessageColor.NEGATIVE + "That cannot be made into blocks!");
        }
        return true;
    }
}
