/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.attributes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdAttributes;
import org.royaldev.royalcommands.rcommands.SubCommand;
import org.royaldev.royalcommands.rcommands.TabCommand;

import com.google.common.collect.Multimap;

public class SCmdRemove extends SubCommand<CmdAttributes> {

    public SCmdRemove(final RoyalCommands instance, final CmdAttributes parent) {
        super(instance, parent, "remove", true, "Remove an attribute attached to the held item", "<command> (attribute)", new String[0], new Short[]{TabCommand.CompletionType.LIST.getShort()});
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        ArrayList<String> attributes = new ArrayList<>();
        final Player p = (Player) cs;
        ItemStack hand = p.getInventory().getItemInMainHand();
        ItemMeta meta = hand.getItemMeta();
        Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
        if (modifiers == null)
            return attributes;
        for (Attribute a : modifiers.keySet()) {
            for (AttributeModifier am : modifiers.get(a)) {
                attributes.add(am.getKey().toString());
            }
        }
        return attributes;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!this.getParent().validHand(cs))
            return true;

        final Player p = (Player) cs;
        ItemStack hand = p.getInventory().getItemInMainHand();
        ItemMeta meta = hand.getItemMeta();
        NamespacedKey nsKey = null;
        if (eargs.length == 1) {
            Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
            for (Attribute a : modifiers.keySet()) {
                nsKey = NamespacedKey.fromString(eargs[0]);
                for (AttributeModifier am : modifiers.get(a)) {
                    if (am.getKey().equals(nsKey)) {
                        meta.removeAttributeModifier(a, am);
                        cs.sendMessage(MessageColor.POSITIVE + "Removed " + MessageColor.NEUTRAL + am.getKey().toString() + MessageColor.POSITIVE + " from " + MessageColor.NEUTRAL + RUtils.getItemName(hand));
                    }
                }
            }
            hand.setItemMeta(meta);
            p.getInventory().setItemInMainHand(hand);
        } else {
            cs.sendMessage(MessageColor.NEGATIVE + "A UUID for a Attribute Modifier must be specified");
        }
        return true;
    }
}
