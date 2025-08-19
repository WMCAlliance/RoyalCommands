/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.exceptions.InvalidItemNameException;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

@ReflectCommand
public class CmdItem extends TabCommand {

    private static final Flag<String> nameFlag = new Flag<>(String.class, "displayname", "name", "n");
    private static final Flag<String> loreFlag = new Flag<>(String.class, "description", "lore", "l");

    public CmdItem(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ITEM_ALIAS.getShort()});
        this.addExpectedFlag(CmdItem.nameFlag);
        this.addExpectedFlag(CmdItem.loreFlag);
    }

    static ItemStack applyMeta(ItemStack is, CommandArguments ca, CommandSender cs) {
        final ItemMeta im = is.getItemMeta();
        if (ca.hasFlag(CmdItem.nameFlag) && RoyalCommands.getInstance().ah.isAuthorized(cs, "rcmds.rename")) {
            final String name = RUtils.colorize(ca.getFlag(CmdItem.nameFlag).getValue());
            if (name == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "You must supply a name.");
                return null;
            }
            im.setDisplayName(name);
        }
        if (ca.hasFlag(CmdItem.loreFlag) && RoyalCommands.getInstance().ah.isAuthorized(cs, "rcmds.lore")) {
            final String loreArgs = ca.getFlag(CmdItem.loreFlag).getValue();
            if (loreArgs == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "You must supply lore.");
                return null;
            }
            final List<String> lore = new ArrayList<>();
            for (final String loreArg : loreArgs.split("(?<!\\\\)\\|")) {
                lore.add(RUtils.colorize(loreArg.replace("\\|", "|")));
            }
            im.setLore(lore);
        }
        // TODO: Enchantments
        //final String[] enchantArgs = ca.getFlag("e", "enchant", "enchants", "enchantment", "enchantments");
        is.setItemMeta(im);
        return is;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        if (eargs.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final Player p = (Player) cs;
        final String item = eargs[0];
        int amount = Config.defaultStack;
        if (eargs.length > 1) {
            try {
                amount = Integer.parseInt(eargs[1]);
            } catch (Exception e) {
                cs.sendMessage(MessageColor.NEGATIVE + "The amount was not a number!");
                return true;
            }
            if (amount < 1) {
                cs.sendMessage(MessageColor.NEGATIVE + "Invalid amount! You must specify a positive amount.");
                return true;
            }
        }
        ItemStack toInv;
        try {
            toInv = RUtils.getItemFromAlias(item, amount);
        } catch (InvalidItemNameException e) {
            toInv = RUtils.getItem(item, amount);
        } catch (NullPointerException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "ItemNameManager was not loaded. Let an administrator know.");
            return true;
        }
        if (toInv == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid item name!");
            return true;
        }
        final Material m = toInv.getType();
        if (m == Material.AIR) {
            cs.sendMessage(MessageColor.NEGATIVE + "You cannot spawn air!");
            return true;
        }
        if (Config.blockedItems.contains(m.name()) && !this.ah.isAuthorized(cs, "rcmds.allowed.item." + m.name())) {
            cs.sendMessage(MessageColor.NEGATIVE + "You are not allowed to spawn that item!");
            this.plugin.getLogger().warning(cs.getName() + " was denied access to the command!");
            return true;
        }
        toInv = CmdItem.applyMeta(toInv, ca, cs);
        if (toInv == null) return true; // display error message in applyMeta

        // TODO This command is very similar to give, it could probably just be a wrapper
        BaseComponent[] message = new ComponentBuilder("Giving ")
                .color(MessageColor.POSITIVE.bc())
                .append(String.valueOf(amount))
                .color(MessageColor.NEUTRAL.bc())
                .append(" of ")
                .color(MessageColor.POSITIVE.bc())
                .append(RUtils.getItemName(m))
                .color(MessageColor.NEUTRAL.bc())
                // .itemTooltip(toInv)
                .append(" to ")
                .color(MessageColor.POSITIVE.bc())
                .append(p.getName())
                .color(MessageColor.NEUTRAL.bc())
                .append(".")
                .color(MessageColor.POSITIVE.bc())
                .create();
        cs.spigot().sendMessage(message);

        if (Config.itemSpawnTag) toInv = RUtils.applySpawnLore(RUtils.setItemStackSpawned(toInv, cs.getName(), true));
        HashMap<Integer, ItemStack> left = p.getInventory().addItem(toInv);
        if (!left.isEmpty() && Config.dropExtras) {
            for (ItemStack i : left.values()) {
                if (Config.itemSpawnTag) i = RUtils.applySpawnLore(RUtils.setItemStackSpawned(i, cs.getName(), true));
                p.getWorld().dropItemNaturally(p.getLocation(), i);
            }
        }
        return true;
    }
}
