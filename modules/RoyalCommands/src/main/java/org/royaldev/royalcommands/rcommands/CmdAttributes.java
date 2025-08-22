/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.attribute.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import com.google.common.collect.Multimap;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

@ReflectCommand
public class CmdAttributes extends TabCommand {

    public CmdAttributes(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.LIST.getShort(), CompletionType.ATTRIBUTE.getShort(), CompletionType.CUSTOM.getShort()});
    }

    @Override
    protected List<String> getCustomCompletions(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        ArrayList<String> endpoints = new ArrayList<>();
        for (AttributeModifier.Operation param : AttributeModifier.Operation.values()) {
            if (!param.name().toLowerCase().startsWith(arg.toLowerCase())) continue;
            endpoints.add(param.name().toLowerCase());
        }
        return endpoints;
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>(Arrays.asList("add", "remove", "clear", "list"));
    }

    @Override
    protected boolean runCommand(CommandSender cs, Command cmd, String label, String[] eargs, CommandArguments ca) {
        if (!(cs instanceof final Player p)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        if (eargs.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            cs.sendMessage(MessageColor.NEGATIVE + "You cannot apply attributes to air!");
            return true;
        }
        ItemMeta meta = hand.getItemMeta();
        Attribute ats = null;
        AttributeModifier.Operation o;
        NamespacedKey nsKey = null;
        double amount;

        String subcommand = eargs[0];
        if (subcommand.equalsIgnoreCase("list")) {
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
        } else if (subcommand.equalsIgnoreCase("clear")) {
            if (meta.hasAttributeModifiers()) {
                cs.sendMessage(MessageColor.POSITIVE + "Clearing Attributes of " + MessageColor.NEUTRAL + hand.getType().name());
                for (Attribute a : meta.getAttributeModifiers().keys()) {
                    meta.removeAttributeModifier(a);
                }
                hand.setItemMeta(meta);
                p.getInventory().setItemInMainHand(hand);
            } else {
                cs.sendMessage(MessageColor.NEGATIVE + "Item has no Attribute Modifiers applied!");
            }

        } else if (subcommand.equalsIgnoreCase("remove")) {
            if (meta.hasAttributeModifiers()) {
                if (eargs.length == 2) {
                    Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
                    for (Attribute a : modifiers.keySet()) {
                        nsKey = NamespacedKey.fromString(eargs[1]);
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
            } else {
                cs.sendMessage(MessageColor.NEGATIVE + "Item has no Attribute Modifiers applied!");
            }

//        } else if (subcommand.equalsIgnoreCase("modify")) { TODO think about this

        } else if (subcommand.equalsIgnoreCase("add")) {
            try {
                ats = Registry.ATTRIBUTE.getOrThrow(NamespacedKey.fromString(eargs[1]));
            } catch (IllegalArgumentException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Enter a valid attribute type!");
                return true;
            }
            try {
                o = AttributeModifier.Operation.valueOf(eargs[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Enter a correct operation for the attribute modifier!");
                return true;
            }
            try {
                amount = Double.parseDouble(eargs[3]);
            } catch (NumberFormatException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Enter a valid modifier amount for the attribute!");
                return true;
            }
            if (eargs.length < 5) {
                try {
                    // String rand = RandomStringUtils.randomAlphanumeric(12);
                    String rand = UUID.randomUUID().toString().replaceAll("-", "");
                    nsKey = NamespacedKey.fromString(rand.substring(0, (int) Math.floor(rand.length() / 2)));
                } catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("Failed to generate attribute key for " + p.getDisplayName());
                }
            } else {
                nsKey = NamespacedKey.fromString(eargs[4]);
            }
            if (ats != null && nsKey != null) {
                meta.addAttributeModifier(ats, new AttributeModifier(nsKey, amount, o, EquipmentSlotGroup.MAINHAND));
                hand.setItemMeta(meta);
                p.getInventory().setItemInMainHand(hand);
                cs.sendMessage(MessageColor.POSITIVE + "The attribute has been applied, with a key of "+ MessageColor.NEUTRAL + nsKey.toString());
            }
        } else cs.sendMessage(MessageColor.NEGATIVE + "No such subcommand!");
        return true;
    }
}
