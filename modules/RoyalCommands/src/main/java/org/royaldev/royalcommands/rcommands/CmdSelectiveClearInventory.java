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
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdSelectiveClearInventory extends TabCommand {

    public CmdSelectiveClearInventory(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort(), CompletionType.ITEM.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 2) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        if (args.length < 3) {
            Player t = this.plugin.getServer().getPlayer(args[0]);
            if (t == null || this.plugin.isVanished(t, cs)) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player is not online!");
                return true;
            }
            if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You cannot alter that player's inventory!");
                return true;
            }
            String called = args[1];
			Material theMaterial = Material.getMaterial(called);
            if (theMaterial != null && theMaterial != Material.AIR) {
                ItemStack toInv;
				toInv = new ItemStack(theMaterial, Config.defaultStack);
                t.getInventory().removeItem(toInv);
                cs.sendMessage(MessageColor.POSITIVE + "Removing " + MessageColor.NEUTRAL + Config.defaultStack + MessageColor.POSITIVE + " of " + MessageColor.NEUTRAL + theMaterial.name().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + " from " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + ".");
                t.sendMessage(MessageColor.POSITIVE + "You have had " + MessageColor.NEUTRAL + Config.defaultStack + MessageColor.POSITIVE + " of " + MessageColor.NEUTRAL + theMaterial.name().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + " taken.");
                return true;
            } else if (theMaterial == Material.AIR) {
                cs.sendMessage(MessageColor.NEGATIVE + "You cannot spawn air!");
                return true;
            } else {
                cs.sendMessage(MessageColor.NEGATIVE + "That block does not exist!");
                return true;
			}
        } else if (args.length == 3) {
            Player target = this.plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player is not online!");
                return true;
            }
            String called = args[1];
			Material theMaterial = Material.getMaterial(called);
            Integer amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (Exception e) {
                cs.sendMessage(MessageColor.NEGATIVE + "The amount was not a number!");
                return true;
            }
            if (amount < 1) {
                amount = 1;
            }
            if (theMaterial == Material.AIR) {
                cs.sendMessage(MessageColor.NEGATIVE + "You cannot spawn air!");
                return true;
            }
            if (Config.blockedItems.contains(theMaterial.name()) && !this.ah.isAuthorized(cs, "rcmds.allowed.item") && !this.ah.isAuthorized(cs, "rcmds.allowed.item." + theMaterial.name().toLowerCase())) {
                cs.sendMessage(MessageColor.NEGATIVE + "You are not allowed to spawn that item!");
                this.plugin.getLogger().warning("[RoyalCommands] " + cs.getName() + " was denied access to the command!");
                return true;
            }
            ItemStack toInv;
            toInv = new ItemStack(Material.getMaterial(called), amount);
            target.getInventory().removeItem(toInv);
            cs.sendMessage(MessageColor.POSITIVE + "Removing " + MessageColor.NEUTRAL + amount + MessageColor.POSITIVE + " of " + MessageColor.NEUTRAL + theMaterial.name().toLowerCase().toString().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + " from " + MessageColor.NEUTRAL + target.getName() + MessageColor.POSITIVE + ".");
            target.sendMessage(MessageColor.POSITIVE + "You have had " + MessageColor.NEUTRAL + amount + MessageColor.POSITIVE + " of " + MessageColor.NEUTRAL + theMaterial.name().toLowerCase().replace("_", " ") + MessageColor.POSITIVE + " taken.");
            return true;
        }
        return true;
    }
}

