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
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.exceptions.InvalidItemNameException;
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;

@ReflectCommand
public class CmdGive extends TabCommand {

    public CmdGive(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort(), CompletionType.ITEM_ALIAS.getShort()});
    }

    public static boolean giveItemStandalone(CommandSender cs, Player target, String itemname, int amount) {
        if (target == null) return false;
        if (amount < 0) {
            target.sendMessage(MessageColor.NEGATIVE + "The amount must be positive!");
            return false;
        }
        ItemStack stack;
        try {
            stack = RUtils.getItemFromAlias(itemname, amount);
        } catch (InvalidItemNameException e) {
            stack = RUtils.getItem(itemname, amount);
        } catch (NullPointerException e) {
            target.sendMessage(MessageColor.NEGATIVE + "ItemNameManager was not loaded. Let an administrator know.");
            return false;
        }
        if (stack == null) {
            target.sendMessage(MessageColor.NEGATIVE + "Invalid item name!");
            return false;
        }
        Material m = stack.getType();
        if (m == Material.AIR) {
            target.sendMessage(MessageColor.NEGATIVE + "You cannot spawn air!");
            return false;
        }
        // @formatter:off
        new FancyMessage("Giving ")
                .color(MessageColor.POSITIVE.cc())
                .then(String.valueOf(amount))
                .color(MessageColor.NEUTRAL.cc())
                .then(" of ")
                .color(MessageColor.POSITIVE.cc())
                .then(RUtils.getItemName(m))
                .color(MessageColor.NEUTRAL.cc())
                .formattedTooltip(RUtils.getItemTooltip(m))
                .then(" to ")
                .color(MessageColor.POSITIVE.cc())
                .then(target.getName())
                .color(MessageColor.NEUTRAL.cc())
                .formattedTooltip(RUtils.getPlayerTooltip(target))
                .then(".")
                .color(MessageColor.POSITIVE.cc())
                .send(target);
        // @formatter:on
        if (Config.itemSpawnTag && cs != null)
            stack = RUtils.applySpawnLore(RUtils.setItemStackSpawned(stack, cs.getName(), true));
        HashMap<Integer, ItemStack> left = target.getInventory().addItem(stack);
        if (!left.isEmpty() && Config.dropExtras) {
            for (ItemStack item : left.values()) {
                if (Config.itemSpawnTag && cs != null)
                    item = RUtils.applySpawnLore(RUtils.setItemStackSpawned(item, cs.getName(), true));
                target.getWorld().dropItemNaturally(target.getLocation(), item);
            }
        }
        return true;
    }

    public static boolean validItem(String itemname) {
        ItemStack stack = RUtils.getItem(itemname, null);
        return stack != null;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (eargs.length < 2) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final Player t = this.plugin.getServer().getPlayer(eargs[0]);
        if (t == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player is not online!");
            return true;
        }
        int amount = Config.defaultStack;
        if (eargs.length == 3) {
            try {
                amount = Integer.parseInt(eargs[2]);
            } catch (Exception e) {
                cs.sendMessage(MessageColor.NEGATIVE + "The amount was not a number!");
                return true;
            }
        }
        if (amount < 1) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid amount! You must specify a positive amount.");
            return true;
        }
        String name = eargs[1];
        ItemStack toInv;
        try {
            toInv = RUtils.getItemFromAlias(name, amount);
        } catch (InvalidItemNameException e) {
            toInv = RUtils.getItem(name, amount);
        } catch (NullPointerException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "ItemNameManager was not loaded. Let an administrator know.");
            return true;
        }
        if (toInv == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid item name!");
            return true;
        }
        Material m = toInv.getType();
        if (m == Material.AIR) {
            cs.sendMessage(MessageColor.NEGATIVE + "You cannot spawn air!");
            return true;
        }
        if (Config.blockedItems.contains(m.name()) && !this.ah.isAuthorized(cs, "rcmds.allowed.item." + m.name())) {
            cs.sendMessage(MessageColor.NEGATIVE + "You are not allowed to spawn that item!");
            this.plugin.getLogger().warning("[RoyalCommands] " + cs.getName() + " was denied access to the command!");
            return true;
        }
        toInv = CmdItem.applyMeta(toInv, ca, cs);
        if (toInv == null) return true; // error message in applyMeta
        if (Config.itemSpawnTag) toInv = RUtils.applySpawnLore(RUtils.setItemStackSpawned(toInv, cs.getName(), true));
        HashMap<Integer, ItemStack> left = t.getInventory().addItem(toInv);
        if (!left.isEmpty() && Config.dropExtras) {
            for (ItemStack item : left.values()) {
                if (Config.itemSpawnTag)
                    item = RUtils.applySpawnLore(RUtils.setItemStackSpawned(item, cs.getName(), true));
                t.getWorld().dropItemNaturally(t.getLocation(), item);
            }
        }
        // @formatter:off
        new FancyMessage("Giving ")
                .color(MessageColor.POSITIVE.cc())
                .then(String.valueOf(amount))
                .color(MessageColor.NEUTRAL.cc())
                .then(" of ")
                .color(MessageColor.POSITIVE.cc())
                .then(RUtils.getItemName(m))
                .color(MessageColor.NEUTRAL.cc())
                .formattedTooltip(RUtils.getItemTooltip(m))
                .then(" to ")
                .color(MessageColor.POSITIVE.cc())
                .then(t.getName())
                .color(MessageColor.NEUTRAL.cc())
                .formattedTooltip(RUtils.getPlayerTooltip(t))
                .then(".")
                .color(MessageColor.POSITIVE.cc())
                .send(cs);
        new FancyMessage("You have been given ")
                .color(MessageColor.POSITIVE.cc())
                .then(String.valueOf(amount))
                .color(MessageColor.NEUTRAL.cc())
                .then(" of ")
                .color(MessageColor.POSITIVE.cc())
                .then(RUtils.getItemName(m))
                .color(MessageColor.NEUTRAL.cc())
                .formattedTooltip(RUtils.getItemTooltip(m))
                .then(".")
                .color(MessageColor.POSITIVE.cc())
                .send(t);
        // @formatter:on
        return true;
    }
}
