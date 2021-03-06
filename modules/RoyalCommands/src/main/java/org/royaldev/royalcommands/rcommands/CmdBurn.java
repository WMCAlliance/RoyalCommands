/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdBurn extends TabCommand {

    public CmdBurn(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        Player t = this.plugin.getServer().getPlayer(args[0]);
        if (t == null || this.plugin.isVanished(t, cs)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You cannot burn that player!");
            return true;
        }
        if (t.getGameMode() == GameMode.CREATIVE) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player is currently in creative mode!");
            return true;
        }
        int len = 5;
        if (args.length > 1) len = RUtils.timeFormatToSeconds(args[1]);
        if (len <= 0) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid time format.");
            return true;
        }
        cs.sendMessage(MessageColor.POSITIVE + "You have set " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + " on fire for " + MessageColor.NEUTRAL + RUtils.formatDateDiff((len * 1000) + System.currentTimeMillis()).substring(1) + MessageColor.POSITIVE + ".");
        t.setFireTicks(len * 20);
        return true;
    }
}
