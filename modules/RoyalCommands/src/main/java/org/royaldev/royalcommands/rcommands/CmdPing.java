/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdPing extends TabCommand {

    public CmdPing(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player) && args.length < 1) {
            cs.sendMessage(MessageColor.POSITIVE + "Pong!");
            return true;
        }
        if (args.length > 0) {
            if (!this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
                RUtils.dispNoPerms(cs);
                return true;
            }
            Player p = this.plugin.getServer().getPlayer(args[0]);
            if (p == null || this.plugin.isVanished(p, cs)) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            int ping = p.getPing();
            String possessive = (p.getName().endsWith("s")) ? "'" : "'s";
            cs.sendMessage(MessageColor.NEUTRAL + p.getName() + possessive + MessageColor.POSITIVE + " ping: " + MessageColor.NEUTRAL + ping + "ms");
            return true;
        }
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.POSITIVE + "Pong!");
            return true;
        }
        Player p = (Player) cs;
        int ping = p.getPing();
        p.sendMessage(MessageColor.POSITIVE + "Your ping: " + MessageColor.NEUTRAL + ping + "ms");
        return true;
    }
}
