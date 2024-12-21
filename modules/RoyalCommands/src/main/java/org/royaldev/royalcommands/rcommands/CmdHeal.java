/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdHeal extends TabCommand {

    public CmdHeal(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            if (!(cs instanceof Player)) {
                cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
                return true;
            }
            Player t = (Player) cs;
            t.sendMessage(MessageColor.POSITIVE + "You have healed yourself!");
            t.setHealth(t.getAttribute(Attribute.MAX_HEALTH).getValue());
            return true;
        }
        Player t = this.plugin.getServer().getPlayer(args[0]);
        if (t == null || this.plugin.isVanished(t, cs)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        cs.sendMessage(MessageColor.POSITIVE + "You have healed " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + ".");
        t.sendMessage(MessageColor.POSITIVE + "You have been healed by " + MessageColor.NEUTRAL + cs.getName() + MessageColor.POSITIVE + "!");
        t.setHealth(t.getAttribute(Attribute.MAX_HEALTH).getValue());
        return true;
    }
}
