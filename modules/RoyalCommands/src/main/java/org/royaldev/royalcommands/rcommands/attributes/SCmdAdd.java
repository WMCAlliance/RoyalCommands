/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdAttributes;
import org.royaldev.royalcommands.rcommands.SubCommand;
import org.royaldev.royalcommands.rcommands.TabCommand;

public class SCmdAdd extends SubCommand<CmdAttributes> {

    public SCmdAdd(final RoyalCommands instance, final CmdAttributes parent) {
        super(instance, parent, "add", true, "Add an attribute to the held item", "<command> (attribute) (operation) (amount)", new String[0], new Short[]{TabCommand.CompletionType.ATTRIBUTE.getShort(), CompletionType.LIST.getShort(), CompletionType.NONE.getShort()});
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        ArrayList<String> endpoints = new ArrayList<>();
        for (AttributeModifier.Operation param : AttributeModifier.Operation.values()) {
            if (!param.name().toLowerCase().startsWith(arg.toLowerCase())) continue;
            endpoints.add(param.name().toLowerCase());
        }
        return endpoints;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!this.getParent().validHand(cs))
            return true;

        final Player p = (Player) cs;
        ItemStack hand = p.getInventory().getItemInMainHand();
        ItemMeta meta = hand.getItemMeta();
        Attribute ats = null;
        AttributeModifier.Operation o;
        NamespacedKey nsKey = null;
        double amount;
        try {
            ats = Registry.ATTRIBUTE.getOrThrow(NamespacedKey.fromString(eargs[0]));
        } catch (IllegalArgumentException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Enter a valid attribute type!");
            return true;
        }
        try {
            o = AttributeModifier.Operation.valueOf(eargs[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Enter a correct operation for the attribute modifier!");
            return true;
        }
        try {
            amount = Double.parseDouble(eargs[2]);
        } catch (NumberFormatException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Enter a valid modifier amount for the attribute!");
            return true;
        }
        if (eargs.length < 4) {
            try {
                // String rand = RandomStringUtils.randomAlphanumeric(12);
                String rand = UUID.randomUUID().toString().replaceAll("-", "");
                nsKey = NamespacedKey.fromString(rand.substring(0, (int) Math.floor(rand.length() / 2)));
            } catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Failed to generate attribute key for " + p.getDisplayName());
            }
        } else {
            nsKey = NamespacedKey.fromString(eargs[3]);
        }
        if (ats != null && nsKey != null) {
            meta.addAttributeModifier(ats, new AttributeModifier(nsKey, amount, o, EquipmentSlotGroup.MAINHAND));
            hand.setItemMeta(meta);
            p.getInventory().setItemInMainHand(hand);
            cs.sendMessage(MessageColor.POSITIVE + "The attribute has been applied, with a key of "+ MessageColor.NEUTRAL + nsKey.toString());
        }
        return true;
    }
}
