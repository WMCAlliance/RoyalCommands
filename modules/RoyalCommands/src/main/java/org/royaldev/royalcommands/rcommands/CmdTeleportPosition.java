/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

@ReflectCommand
public class CmdTeleportPosition extends TabCommand {

    public CmdTeleportPosition(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.NONE.getShort(), CompletionType.NONE.getShort(), CompletionType.NONE.getShort(), CompletionType.WORLD.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player) && args.length < 5) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        if (args.length < 3) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final Double x = RUtils.getDouble(args[0]);
        final Double y = RUtils.getDouble(args[1]);
        final Double z = RUtils.getDouble(args[2]);
        if (x == null || y == null || z == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "One of the coordinates was invalid.");
            return true;
        }
        final Player p = (cs instanceof Player) ? (Player) cs : null;
        final Player toTeleport = (args.length > 4) ? this.plugin.getServer().getPlayer(args[4]) : p;
        if (toTeleport == null || this.plugin.isVanished(toTeleport, cs)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        final Location pLoc;
        World w = toTeleport.getWorld();
        if (args.length > 3) w = this.plugin.getServer().getWorld(args[3]);
        if (w == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "That world does not exist!");
            return true;
        }
        pLoc = new Location(w, x, y, z);
        if (!toTeleport.getName().equals(cs.getName())) {
            cs.sendMessage(MessageColor.POSITIVE + "Teleporting " + MessageColor.NEUTRAL + toTeleport.getName() + MessageColor.POSITIVE + " to x: " + MessageColor.NEUTRAL + x + MessageColor.POSITIVE + ", y: " + MessageColor.NEUTRAL + y + MessageColor.POSITIVE + ", z: " + MessageColor.NEUTRAL + z + MessageColor.POSITIVE + " in world " + MessageColor.NEUTRAL + RUtils.getMVWorldName(w) + MessageColor.POSITIVE + ".");
        }
        toTeleport.sendMessage(MessageColor.POSITIVE + "Teleporting you to x: " + MessageColor.NEUTRAL + x + MessageColor.POSITIVE + ", y: " + MessageColor.NEUTRAL + y + MessageColor.POSITIVE + ", z: " + MessageColor.NEUTRAL + z + MessageColor.POSITIVE + " in world " + MessageColor.NEUTRAL + RUtils.getMVWorldName(w) + MessageColor.POSITIVE + ".");
        final RPlayer rp = MemoryRPlayer.getRPlayer(toTeleport);
        final String error = rp.getTeleporter().teleport(pLoc);
        if (!error.isEmpty()) {
            toTeleport.sendMessage(MessageColor.NEGATIVE + error);
            return true;
        }
        return true;
    }
}
