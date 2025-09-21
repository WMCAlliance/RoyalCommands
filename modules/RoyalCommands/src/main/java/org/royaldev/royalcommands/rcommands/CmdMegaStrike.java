/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdMegaStrike extends TabCommand {

    public CmdMegaStrike(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player) && args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        if (args.length < 1 && cs instanceof Player) {
            Player p = (Player) cs;
            BlockIterator b = new BlockIterator(p, 0);
            if (!b.hasNext()) {
                cs.sendMessage(MessageColor.NEGATIVE + "Cannot megastrike there!");
                return true;
            }
            Block bb = b.next();
            while (b.hasNext()) {
                if (RUtils.isBlockAir(b.next().getType())) continue;
                bb = b.next();
                break;
            }
            for (int i = 0; i < 15; i++) p.getWorld().strikeLightning(bb.getLocation());
            return true;
        } else {
            if (!this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission for that!");
                return true;
            }
            Player target = this.plugin.getServer().getPlayer(args[0]);
            if (target == null || this.plugin.isVanished(target, cs)) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            cs.sendMessage(MessageColor.POSITIVE + "Megasmiting " + MessageColor.NEUTRAL + target.getName() + MessageColor.POSITIVE + ".");
            target.sendMessage(MessageColor.NEGATIVE + "You have been megasmited by " + MessageColor.NEUTRAL + cs.getName() + MessageColor.NEGATIVE + ".");
            for (int i = 0; i < 15; i++) target.getWorld().strikeLightning(target.getLocation());
            return true;
        }
    }
}
