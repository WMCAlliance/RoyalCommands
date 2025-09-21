/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.attributes;

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

import com.google.common.collect.Multimap;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;


public class SCmdList extends SubCommand<CmdAttributes> {

    public SCmdList(final RoyalCommands instance, final CmdAttributes parent) {
        super(instance, parent, "list", true, "List all attributes attached to the held item", "<command>", new String[0], new Short[0]);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!this.getParent().validHand(cs))
            return true;

        final Player p = (Player) cs;
        ItemStack hand = p.getInventory().getItemInMainHand();
        ItemMeta meta = hand.getItemMeta();

        if (meta.hasAttributeModifiers()) {
            final BaseComponent[] bc = new ComponentBuilder("Item ")
                    .color(MessageColor.POSITIVE.bc())
                    .append(RUtils.getItemName(hand))
                    .color(MessageColor.NEUTRAL.bc())
                    .append(" has these Attribute Modifiers:")
                    .color(MessageColor.POSITIVE.bc())
                    .create();
            cs.spigot().sendMessage(bc);
            Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
            for (Attribute a : modifiers.keySet()) {
                String name = new TranslatableComponent(a.getTranslationKey()).toPlainText();
                for (AttributeModifier am : modifiers.get(a)) {
                    String operation = RUtils.getFriendlyEnumName(am.getOperation());
                    Text tt = new Text(
                            MessageColor.POSITIVE + "Attribute: " + MessageColor.NEUTRAL + name + "\n" +
                            MessageColor.POSITIVE + "Operation: " + MessageColor.NEUTRAL + operation + "\n" +
                            MessageColor.POSITIVE + "Amount: " + MessageColor.NEUTRAL + am.getAmount() + "\n" +
                            MessageColor.POSITIVE + "Key: " + MessageColor.NEUTRAL + am.getKey() + "\n" +
                            MessageColor.NEUTRAL + "Click to remove");
                    BaseComponent[] bca = new ComponentBuilder("  " + name + " " + operation + " " + am.getAmount())
                            .color(MessageColor.NEUTRAL.bc())
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tt))
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/attr remove " + am.getKey().toString()))
                            .create();
                    cs.spigot().sendMessage(bca);
                }
            }
        } else {
            cs.sendMessage(MessageColor.NEGATIVE + "Item has no Attribute Modifiers applied!");
        }
        return true;
    }
}
