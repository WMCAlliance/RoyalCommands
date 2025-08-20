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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
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
        UUID uuid = null;
        double amount;

        String subcommand = eargs[0];
        if (subcommand.equalsIgnoreCase("list")) {
            if (meta.hasAttributeModifiers()) {
                final BaseComponent[] bc = new ComponentBuilder("Item ")
                        .color(MessageColor.POSITIVE.bc())
                        .append(hand.getType().name())
                        .color(MessageColor.NEUTRAL.bc())
                        .append(" has these Attribute Modifiers:")
                        .color(MessageColor.POSITIVE.bc())
                        .create();
                cs.spigot().sendMessage(bc);
                for (AttributeModifier am : meta.getAttributeModifiers().values()) {
                    // cs.sendMessage(am.toString());
                    Text tt = new Text(MessageColor.NEUTRAL
                            + "Attribute: " + MessageColor.RESET + am.getKey().toString() + "\n" + MessageColor.NEUTRAL
                            + "Operation: " + MessageColor.RESET + am.getOperation() + "\n" + MessageColor.NEUTRAL
                            + "Amount: " + MessageColor.RESET + am.getAmount() + "\n" + MessageColor.NEUTRAL
                            + "UUID: " + MessageColor.RESET + am.getKey() + "\n"
                            + "click to copy the UUID");
                    BaseComponent[] bca = new ComponentBuilder(am.getKey().toString())
                            .color(MessageColor.NEUTRAL.bc())
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tt))
                            .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(am.getKey())))
                            .create();
                    cs.spigot().sendMessage(bca);
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
                    uuid = UUID.fromString(eargs[1]);
                    for (AttributeModifier am : meta.getAttributeModifiers().values()) {
                        if (am.getUniqueId().equals(uuid)) {
                            meta.removeAttributeModifier(Attribute.valueOf(am.getName()));
                            cs.sendMessage(MessageColor.POSITIVE + "Removing " + MessageColor.NEUTRAL + am.getName() + MessageColor.POSITIVE + " from " + MessageColor.NEUTRAL + hand.getType().name());
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
                    uuid = UUID.randomUUID();
                } catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("UUID failed for " + p.getDisplayName());
                }
            } else {
                uuid = UUID.fromString(eargs[3]);
            }
            if (ats != null && uuid != null) {
                meta.addAttributeModifier(ats, new AttributeModifier(uuid, ats.name(), amount, o, EquipmentSlot.HAND));
                hand.setItemMeta(meta);
                p.getInventory().setItemInMainHand(hand);
                cs.sendMessage(MessageColor.POSITIVE + "The attribute has been applied, it has a UUID of  "+ MessageColor.NEUTRAL + uuid);
            }
        } else cs.sendMessage(MessageColor.NEGATIVE + "No such subcommand!");
        return true;
    }
}
