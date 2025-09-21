/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
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
import org.royaldev.royalcommands.spawninfo.SpawnInfo;
import org.royaldev.royalcommands.spawninfo.SpawnInfo.SpawnInfoManager;

@ReflectCommand
public class CmdSpawnInfo extends TabCommand {

    public CmdSpawnInfo(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.LIST.getShort()});
    }

    @Override
    protected List<String> customList(CommandSender cs, Command cmd, String label, String[] args, String arg) {
        return Arrays.asList("check", "set", "remove", "help");
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
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (RUtils.isBlockAir(hand.getType())) {
            cs.sendMessage(MessageColor.NEGATIVE + "You must be holding something to use this command.");
            return true;
        }
        final SpawnInfo si = SpawnInfoManager.getSpawnInfo(hand);
        final String subcommand = eargs[0];
        if (subcommand.equalsIgnoreCase("check")) {
            cs.sendMessage(MessageColor.POSITIVE + "Spawn information on " + MessageColor.NEUTRAL + RUtils.getItemName(hand) + MessageColor.POSITIVE + ":");
            cs.sendMessage(MessageColor.POSITIVE + "  Is spawned: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(si.isSpawned()));
            if (si.isSpawned())
                cs.sendMessage(MessageColor.POSITIVE + "  Spawned by: " + MessageColor.NEUTRAL + si.getSpawner());
            cs.sendMessage(MessageColor.POSITIVE + "  Made with spawned items: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(si.hasComponents()));
            if (si.hasComponents()) {
                cs.sendMessage(MessageColor.POSITIVE + "  Components:");
                for (String component : si.getComponents())
                    cs.sendMessage(MessageColor.POSITIVE + "    - " + MessageColor.NEUTRAL + component);
            }
            return true;
        } else if (subcommand.equalsIgnoreCase("set")) {
            cs.sendMessage(MessageColor.NEGATIVE + "This mode has not yet been implemented!");
            return true;
        } else if (subcommand.equalsIgnoreCase("remove")) {
            final ItemMeta im = hand.getItemMeta();
            if (im.hasLore()) {
                final List<String> lore = im.getLore();
                for (String add : Config.itemSpawnTagLore) {
                    add = RUtils.colorize(add);
                    if (!lore.contains(add)) continue;
                    lore.remove(add);
                }
                im.setLore(lore);
            }
            hand.setItemMeta(im);
            p.getInventory().setItemInMainHand(SpawnInfoManager.removeSpawnInfo(hand));
            cs.sendMessage(MessageColor.POSITIVE + "Spawn information removed from the item in hand.");
            return true;
        } else if (subcommand.equalsIgnoreCase("help") || subcommand.equals("?")) {
            cs.sendMessage(MessageColor.POSITIVE + "RoyalCommands SpawnInfo Help");
            cs.sendMessage(MessageColor.POSITIVE + "===============================");
            cs.sendMessage("  " + MessageColor.NEUTRAL + "/" + label + " check" + MessageColor.POSITIVE + " - Checks for and displays spawn information.");
            cs.sendMessage("  " + MessageColor.NEUTRAL + "/" + label + " set" + MessageColor.POSITIVE + " - Sets and edits spawn information.");
            cs.sendMessage("  " + MessageColor.NEUTRAL + "/" + label + " remove" + MessageColor.POSITIVE + " - Removes all spawn information from an item.");
            cs.sendMessage("  " + MessageColor.NEUTRAL + "/" + label + " help" + MessageColor.POSITIVE + " - Displays this help.");
            return true;
        } else {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid subcommand.");
            cs.sendMessage(MessageColor.NEGATIVE + "Try " + MessageColor.NEUTRAL + "/" + label + " help" + MessageColor.NEGATIVE + ".");
            return true;
        }
    }
}
