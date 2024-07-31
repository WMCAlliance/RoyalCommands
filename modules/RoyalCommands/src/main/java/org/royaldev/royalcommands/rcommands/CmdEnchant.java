/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.*;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdEnchant extends TabCommand {

    public CmdEnchant(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ENCHANT.getShort()});
    }

    /**
     * Gets the real level of an enchantment to be applied to an item.
     * <br>
     * Keep in mind that...
     * <ul>
     * <li>-1 is the maximum level of the enchantment</li>
     * <li>-2 is the maximum <em>short</em> value in Java</li>
     * <li>0 will return 0 for removal</li>
     * <li>Any other number will be returned without modification</li>
     * </ul>
     *
     * @param e Enchantment being added
     * @param i Level supplied (-1, -2, 0, 10, etc.)
     * @return Real level
     */
    private int getRealLevel(Enchantment e, int i) {
        switch (i) {
            case -1: // Max level
                return e.getMaxLevel();
            case -2: // Max short value
                return Short.MAX_VALUE;
            default:
                return i;
        }
    }

    private void sendEnchantmentList(CommandSender cs) {
        StringBuilder sb = new StringBuilder();
        List<Enchantment> ench = Registry.ENCHANTMENT.stream().toList();
        ench.sort(new Comparator<Enchantment>() {
            @Override
            public int compare(Enchantment e1, Enchantment e2) {
              return e1.getKey().getKey().compareTo(e1.getKey().getKey());
            }
        });
        for (Enchantment e : ench) {
            sb.append(MessageColor.NEUTRAL);
            sb.append(e.getKey().getKey());
            sb.append(MessageColor.RESET);
            sb.append(", ");
        }
        cs.sendMessage(sb.substring(0, sb.length() - 4)); // "&r, " = 4
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String args[], CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        if (args.length < 1) {
            sendEnchantmentList(cs);
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        Player p = (Player) cs;
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            cs.sendMessage(MessageColor.NEGATIVE + "Air cannot be enchanted!");
            return true;
        }
        Enchantment toAdd = Registry.ENCHANTMENT.get(new NamespacedKey("minecraft", args[0]));
        if (toAdd == null) {
            if (args[0].equalsIgnoreCase("all")) {
                int level;
                if (args.length < 2) level = -1;
                else if (args.length > 1 && args[1].equalsIgnoreCase("max")) level = -2;
                else {
                    try {
                        level = Short.parseShort(args[1]);
                    } catch (NumberFormatException e) {
                        cs.sendMessage(MessageColor.NEGATIVE + "The level supplied was not a number or greater than " + Short.MAX_VALUE + "!");
                        return true;
                    }
                    if (level < 0) {
                        cs.sendMessage(MessageColor.NEGATIVE + "The level cannot be below zero!");
                        return true;
                    }
                    if (level > Short.MAX_VALUE) {
                        cs.sendMessage(MessageColor.NEGATIVE + "The level cannot be above " + Short.MAX_VALUE + "!");
                        return true;
                    }
                }
                if (level == 0) {
                    for (Enchantment e : Registry.ENCHANTMENT) {
                        if (!hand.containsEnchantment(e)) continue;
                        hand.removeEnchantment(e);
                    }
                    cs.sendMessage(MessageColor.POSITIVE + "Removed all enchantments from " + MessageColor.NEUTRAL + RUtils.getItemName(hand) + MessageColor.POSITIVE + ".");
                } else {
                    if (level == -2 && !this.ah.isAuthorized(cs, "rcmds.enchant.levels")) {
                        cs.sendMessage(MessageColor.NEGATIVE + "You cannot apply levels for enchantments higher than the maximum vanilla level!");
                        return true;
                    }
                    boolean skipped = false;
                    for (Enchantment e : Registry.ENCHANTMENT) {
                        int toApply = getRealLevel(e, level);
                        if (toApply > e.getMaxLevel() && !this.ah.isAuthorized(cs, "rcmds.enchant.levels")) {
                            skipped = true;
                            continue;
                        }
                        if (!e.canEnchantItem(hand) && !this.ah.isAuthorized(cs, "rcmds.enchant.illegal")) {
                            skipped = true;
                            continue;
                        }
                        hand.addUnsafeEnchantment(e, toApply);
                    }
                    if (skipped)
                        cs.sendMessage(MessageColor.NEGATIVE + "Some enchantments were not applied because you do not have permission for them.");
                    String atLevel;
                    if (level == -1) atLevel = "their maximum levels";
                    else if (level == -2) atLevel = "the maximum possible level";
                    else atLevel = "level " + String.valueOf(level);
                    cs.sendMessage(MessageColor.POSITIVE + "Added " + MessageColor.NEUTRAL + "all" + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + RUtils.getItemName(hand) + MessageColor.POSITIVE + " at " + MessageColor.NEUTRAL + atLevel + MessageColor.POSITIVE + ".");
                }
                return true;
            }
            sendEnchantmentList(cs);
            cs.sendMessage(MessageColor.NEGATIVE + "No such enchantment!");
            return true;
        }
        int level;
        if (args.length < 2) level = -1;
        else if (args.length > 1 && args[1].equalsIgnoreCase("max")) level = -2;
        else {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "The level supplied was not a number or greater than " + Integer.MAX_VALUE + "!");
                return true;
            }
            if (level < 0) {
                cs.sendMessage(MessageColor.NEGATIVE + "The level cannot be below zero!");
                return true;
            }
            if (level > Short.MAX_VALUE) {
                cs.sendMessage(MessageColor.NEGATIVE + "The level cannot be above " + Short.MAX_VALUE + "!");
                return true;
            }
        }
        if (level == 0) {
            if (!hand.containsEnchantment(toAdd)) {
                cs.sendMessage(MessageColor.NEGATIVE + "That " + MessageColor.NEUTRAL + RUtils.getItemName(hand) + MessageColor.POSITIVE + " does not contain " + MessageColor.NEUTRAL + toAdd.getKey().getKey().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + ".");
                return true;
            }
            hand.removeEnchantment(toAdd);
            cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + toAdd.getKey().getKey().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + " from " + MessageColor.NEUTRAL + RUtils.getItemName(hand) + MessageColor.POSITIVE + ".");
        } else {
            int toApply = getRealLevel(toAdd, level);
            if (toApply > toAdd.getMaxLevel() && !this.ah.isAuthorized(cs, "rcmds.enchant.levels")) {
                cs.sendMessage(MessageColor.NEGATIVE + "That level is too high for " + MessageColor.NEUTRAL + toAdd.getKey().getKey().replace("_", " ").toLowerCase() + MessageColor.NEGATIVE + ".");
                return true;
            }
            if (!toAdd.canEnchantItem(hand) && !this.ah.isAuthorized(cs, "rcmds.enchant.illegal")) {
                cs.sendMessage(MessageColor.NEGATIVE + "Cannot add " + MessageColor.NEUTRAL + toAdd.getKey().getKey().replace("_", " ").toLowerCase() + MessageColor.NEGATIVE + " because it is not for that type of item!");
                return true;
            }
            if (!this.ah.isAuthorized(cs, "rcmds.enchant.illegal"))
                for (Enchantment e : hand.getEnchantments().keySet()) {
                    if (toAdd.conflictsWith(e)) {
                        cs.sendMessage(MessageColor.NEGATIVE + "Cannot add " + MessageColor.NEUTRAL + toAdd.getKey().getKey().replace("_", " ").toLowerCase() + MessageColor.NEGATIVE + " because it conflicts with " + MessageColor.NEUTRAL + e.getKey().getKey().replace("_", " ").toLowerCase() + MessageColor.NEGATIVE + ".");
                        return true;
                    }
                }
            hand.addUnsafeEnchantment(toAdd, toApply);
            cs.sendMessage(MessageColor.POSITIVE + "Added " + MessageColor.NEUTRAL + toAdd.getKey().getKey().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + RUtils.getItemName(hand) + MessageColor.POSITIVE + " at level " + MessageColor.NEUTRAL + toApply + MessageColor.POSITIVE + ".");
        }
        return true;
    }
}
