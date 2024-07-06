/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdSlap extends TabCommand {

    public CmdSlap(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        Player t;
        t = this.plugin.getServer().getPlayer(args[0]);
        if (t == null || this.plugin.isVanished(t, cs)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That person is not online!");
            return true;
        }
        if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You may not slap that player.");
            return true;
        }
        Random r = new Random();
        Vector push = new Vector();
        push.setY(r.nextInt(2));
        push.setX(r.nextInt(4) - 2);
        push.setZ(r.nextInt(4) - 2);
        t.sendHurtAnimation(r.nextInt(270));
        t.setVelocity(push);
        this.plugin.getServer().broadcastMessage(ChatColor.GOLD + cs.getName() + MessageColor.RESET + " slaps " + MessageColor.NEGATIVE + t.getName() + MessageColor.RESET + "!");
        return true;
    }
}
