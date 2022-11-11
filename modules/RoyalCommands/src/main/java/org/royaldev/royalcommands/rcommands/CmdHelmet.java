/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.exceptions.InvalidItemNameException;

@ReflectCommand
public class CmdHelmet extends TabCommand {

    public CmdHelmet(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ITEM_ALIAS.getShort()});
    }
    /**
     * TODO: Add support to add a helmet to another player
     */
    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        String name;
        Player p = (Player) cs;
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        if (args.length < 1) {
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand.getType() == Material.AIR) {
                cs.sendMessage(MessageColor.NEGATIVE + "You cannot use air as a helmet!");
                return true;
            }
            name = hand.getType().name();
        } else {
            name = args[0];
        }
        if (name.equalsIgnoreCase("none")) {
            ItemStack helm = p.getInventory().getHelmet();
            if (Config.requireHelm) {
                if (pcm.getString("helmet") != null) {
                    ItemStack stack = RUtils.getItem(pcm.getString("helmet"), 1);
                    if (stack == null) {
                        cs.sendMessage(MessageColor.NEGATIVE + "You have no helmet!");
                        return true;
                    }
                    if (helm == null || helm.getType() == Material.AIR || helm.getType() != stack.getType()) {
                        p.sendMessage(MessageColor.NEGATIVE + "You have already removed your helmet!");
                        pcm.set("helmet", null);
                        return true;
                    }
                    p.getInventory().addItem(stack);
                }
            }
            p.getInventory().setHelmet(null);
            p.sendMessage(MessageColor.POSITIVE + "Removed your helmet.");
            return true;
        }
        ItemStack stack = RUtils.getItem(name, 1);
        if (stack == null) {
            try {
                stack = RUtils.getItemFromAlias(name, 1);
            } catch (InvalidItemNameException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Invalid item name!");
                return true;
            } catch (NullPointerException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "ItemNameManager was not loaded. Let an administrator know.");
                return true;
            }
        }
        if (stack == null) {
            p.sendMessage(MessageColor.NEGATIVE + "Invalid item name!");
            return true;
        }
        if (Config.requireHelm) {
            if (!p.getInventory().contains(stack)) {
                p.sendMessage(MessageColor.NEGATIVE + "You don't have that item!");
                return true;
            }
            pcm.set("helmet", stack.getType().name());
            p.getInventory().remove(stack);
        }
        p.getInventory().setHelmet(stack);
        p.sendMessage(MessageColor.POSITIVE + "Set your helmet to " + MessageColor.NEUTRAL + RUtils.getFriendlyEnumName(stack.getType()) + MessageColor.POSITIVE + ".");
        return true;
    }
}
