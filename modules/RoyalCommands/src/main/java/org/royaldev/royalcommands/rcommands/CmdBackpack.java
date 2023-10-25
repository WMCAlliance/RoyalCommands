/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

@ReflectCommand
public class CmdBackpack extends TabCommand {

    public CmdBackpack(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{TabCommand.CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, final CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        final Player p = (Player) cs;
        if (args.length > 0) {
            if (!this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
                RUtils.dispNoPerms(cs);
                return true;
            }
            if (args.length < 2) {
                cs.sendMessage(MessageColor.NEGATIVE + "No world specified.");
                return true;
            }
            final World w = this.plugin.getServer().getWorld(args[1]);
            if (w == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "No such world!");
                return true;
            }
            final OfflinePlayer t = RUtils.getOfflinePlayer(args[0]);
            if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
                RUtils.dispNoPerms(cs, MessageColor.NEGATIVE + "You cannot access that player's backpack!");
                return true;
            }
            final RPlayer rp = MemoryRPlayer.getRPlayer(t.getUniqueId());
            final Inventory i = rp.getBackpack(w);
            if (i == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            p.openInventory(i);
            return true;
        }
        final RPlayer rp = MemoryRPlayer.getRPlayer(p);
        final Inventory i = rp.getBackpack(p.getWorld());
        p.openInventory(i);
        return true;
    }
}
