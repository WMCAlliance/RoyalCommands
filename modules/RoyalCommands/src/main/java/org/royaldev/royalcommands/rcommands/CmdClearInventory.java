/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.WorldManager;

@ReflectCommand
public class CmdClearInventory extends TabCommand {

    public CmdClearInventory(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{TabCommand.CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1 && !(cs instanceof Player)) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final OfflinePlayer t = (args.length > 0) ? RUtils.getOfflinePlayer(args[0]) : (OfflinePlayer) cs;
        if (!t.getName().equalsIgnoreCase(cs.getName()) && !this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission to clear other players' inventories.");
            return true;
        }
        if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT) && !t.getName().equalsIgnoreCase(cs.getName())) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't clear that player's inventory.");
            return true;
        }
        String world = null;
        if (t.isOnline()) world = ((Player) t).getWorld().getName();
        else if (args.length > 1) world = args[1];
        if (world == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "You need to specify a world to clear the inventory on!");
            return true;
        }
        if (!t.isOnline() && !Config.separateInv) {
            cs.sendMessage(MessageColor.NEGATIVE + "Cannot modify offline inventories unless inventory separation is on!");
            return true;
        } else if (t.isOnline() && !Config.separateInv) {
            final Player p = (Player) t;
            p.getInventory().clear();
            cs.sendMessage(MessageColor.POSITIVE + "You have cleared the inventory of " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + " in world " + MessageColor.NEUTRAL + RUtils.getMVWorldName(RUtils.getWorld(world)) + MessageColor.POSITIVE + ".");
            if (!p.getName().equalsIgnoreCase(cs.getName()))
                p.sendMessage(MessageColor.POSITIVE + "Your inventory for " + MessageColor.NEUTRAL + world + MessageColor.POSITIVE + " has been cleared by " + MessageColor.NEUTRAL + cs.getName() + MessageColor.POSITIVE + ".");
            return true;
        }
        final Inventory i = WorldManager.il.getOfflinePlayerInventory(t, world);
        if (i == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "No inventory found.");
            return true;
        }
        i.clear();
        WorldManager.il.saveInventory(t, world, i);
        if (t.isOnline()) {
            final Player p = (Player) t;
            if (p.getWorld().getName().equalsIgnoreCase(world)) p.getInventory().setContents(i.getContents());
        }
        cs.sendMessage(MessageColor.POSITIVE + "You have cleared the inventory of " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + " in world " + MessageColor.NEUTRAL + RUtils.getMVWorldName(RUtils.getWorld(world)) + MessageColor.POSITIVE + ".");
        if (t.isOnline() && !t.getName().equalsIgnoreCase(cs.getName()))
            ((Player) t).sendMessage(MessageColor.POSITIVE + "Your inventory for " + MessageColor.NEUTRAL + world + MessageColor.POSITIVE + " has been cleared by " + MessageColor.NEUTRAL + cs.getName() + MessageColor.POSITIVE + ".");
        return true;
    }
}
