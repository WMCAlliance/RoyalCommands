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

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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


        TextComponent tcS = new TextComponent("Giving ");
        tcS.setColor(MessageColor.POSITIVE.bc());

        TextComponent tc2 = new TextComponent(String.valueOf(amount));
        tc2.setColor(MessageColor.NEUTRAL.bc());
        tcS.addExtra(tc2.duplicate());

        tc2.setText(" of ");
        tc2.setColor(MessageColor.POSITIVE.bc());
        tcS.addExtra(tc2.duplicate());

        TextComponent tci = new TextComponent(RUtils.getItemName(stack));
        tci.setColor(MessageColor.NEUTRAL.bc());
        tci.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, RUtils.getItemTooltip(m)));
        tcS.addExtra(tci);

        tc2.setText(" to ");
        tc2.setColor(MessageColor.POSITIVE.bc());
        tcS.addExtra(tc2.duplicate());

        TextComponent tcp = new TextComponent(target.getDisplayName());
        tcp.setColor(MessageColor.NEUTRAL.bc());
        tcp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RUtils.getPlayerTooltip(target)));
        tcS.addExtra(tcp);

        tc2.setText(".");
        tc2.setColor(MessageColor.POSITIVE.bc());
        tcS.addExtra(tc2.duplicate());

        cs.spigot().sendMessage(tcS);
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

        TextComponent tcR = new TextComponent("You have been given ");
        TextComponent tcS = new TextComponent("Giving ");
        tcR.setColor(MessageColor.POSITIVE.bc());
        tcS.setColor(MessageColor.POSITIVE.bc());

        TextComponent tc2 = new TextComponent(String.valueOf(amount));
        tc2.setColor(MessageColor.NEUTRAL.bc());
        tcR.addExtra(tc2.duplicate());
        tcS.addExtra(tc2.duplicate());

        tc2.setText(" of ");
        tc2.setColor(MessageColor.POSITIVE.bc());
        tcR.addExtra(tc2.duplicate());
        tcS.addExtra(tc2.duplicate());

        TextComponent tci = new TextComponent(RUtils.getItemName(toInv));
        tci.setColor(MessageColor.NEUTRAL.bc());
        tci.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, RUtils.getItemTooltip(m)));
        tcR.addExtra(tci);
        tcS.addExtra(tci);

        tc2.setText(" to ");
        tc2.setColor(MessageColor.POSITIVE.bc());
        tcS.addExtra(tc2.duplicate());

        TextComponent tcp = new TextComponent(t.getDisplayName());
        tcp.setColor(MessageColor.NEUTRAL.bc());
        tcp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RUtils.getPlayerTooltip(t)));
        tcS.addExtra(tcp);

        tc2.setText(".");
        tc2.setColor(MessageColor.POSITIVE.bc());
        tcR.addExtra(tc2.duplicate());
        tcS.addExtra(tc2.duplicate());


        t.spigot().sendMessage(tcR);

        if (!(cs instanceof Player) || !t.equals((Player)cs))
            cs.spigot().sendMessage(tcS);

        return true;
    }
}
