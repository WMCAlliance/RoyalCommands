/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

@ReflectCommand
public class CmdJump extends TabCommand {

    public CmdJump(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        Block bb = RUtils.getTarget(p);
        if (bb == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "Can't jump there!");
            return true;
        }
        Location bLoc = new Location(p.getWorld(), bb.getLocation().getX() + .5, bb.getLocation().getY() + 1, bb.getLocation().getZ() + .5, p.getLocation().getYaw(), p.getLocation().getPitch());
        final RPlayer rp = MemoryRPlayer.getRPlayer(p);
        String error = rp.getTeleporter().teleport(bLoc);
        if (!error.isEmpty()) {
            p.sendMessage(MessageColor.NEGATIVE + error);
            return true;
        }
        return true;
    }
}
