/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.text.DecimalFormat;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdCoords extends TabCommand {

    public CmdCoords(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
		DecimalFormat df = new DecimalFormat("#.###");
        if (!(cs instanceof Player) && args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        if (args.length < 1 && cs instanceof Player) {
            Player p = (Player) cs;
            Location l = p.getLocation();
            cs.sendMessage(MessageColor.POSITIVE + "x: " + MessageColor.NEUTRAL + df.format(l.getX()));
            cs.sendMessage(MessageColor.POSITIVE + "y: " + MessageColor.NEUTRAL + df.format(l.getY()));
            cs.sendMessage(MessageColor.POSITIVE + "z: " + MessageColor.NEUTRAL + df.format(l.getZ()));
            cs.sendMessage(MessageColor.POSITIVE + "pitch: " + MessageColor.NEUTRAL + df.format(l.getPitch()));
            cs.sendMessage(MessageColor.POSITIVE + "yaw: " + MessageColor.NEUTRAL + df.format(l.getYaw()));
            cs.sendMessage(MessageColor.POSITIVE + "world: " + MessageColor.NEUTRAL + RUtils.getMVWorldName(l.getWorld()));
            return true;
        }
        if (!this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
            RUtils.dispNoPerms(cs);
            return true;
        }
        Player t = this.plugin.getServer().getPlayer(args[0]);
        if (t == null || this.plugin.isVanished(t, cs)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        Location l = t.getLocation();
        cs.sendMessage(MessageColor.POSITIVE + "x: " + MessageColor.NEUTRAL + df.format(l.getX()));
        cs.sendMessage(MessageColor.POSITIVE + "y: " + MessageColor.NEUTRAL + df.format(l.getY()));
        cs.sendMessage(MessageColor.POSITIVE + "z: " + MessageColor.NEUTRAL + df.format(l.getZ()));
        cs.sendMessage(MessageColor.POSITIVE + "pitch: " + MessageColor.NEUTRAL + df.format(l.getPitch()));
        cs.sendMessage(MessageColor.POSITIVE + "yaw: " + MessageColor.NEUTRAL + df.format(l.getYaw()));
        cs.sendMessage(MessageColor.POSITIVE + "world: " + MessageColor.NEUTRAL + RUtils.getMVWorldName(l.getWorld()));
        return true;
    }
}
