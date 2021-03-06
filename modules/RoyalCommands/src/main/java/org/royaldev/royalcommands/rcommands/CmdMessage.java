/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdMessage extends TabCommand {

    public static final HashMap<String, String> replydb = new HashMap<>();

    public CmdMessage(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{TabCommand.CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 2) {
            return false;
        }
        final Player t = this.plugin.getServer().getPlayer(args[0]);
        final String m = RoyalCommands.getFinalArg(args, 1).trim();
        if (t == null || "".equals(t.getName().trim())) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player is not online!");
            return true;
        }
        if (this.plugin.isVanished(t, cs)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        synchronized (replydb) {
            replydb.put(t.getName(), cs.getName());
            replydb.put(cs.getName(), t.getName());
        }

        if ("".equals(m)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You entered no message!");
            return true;
        }
        t.sendMessage(MessageColor.NEUTRAL + "[" + MessageColor.POSITIVE + cs.getName() + MessageColor.NEUTRAL + " -> " + MessageColor.POSITIVE + "You" + MessageColor.NEUTRAL + "] " + m);
        cs.sendMessage(MessageColor.NEUTRAL + "[" + MessageColor.POSITIVE + "You" + MessageColor.NEUTRAL + " -> " + MessageColor.POSITIVE + t.getName() + MessageColor.NEUTRAL + "] " + m);
        if (!this.ah.isAuthorized(cs, cmd, PermType.EXEMPT)) {
            for (final Player p1 : this.plugin.getServer().getOnlinePlayers()) {
                if (PlayerConfigurationManager.getConfiguration(p1).getBoolean("messagespy")) {
                    if (t == p1 || cs == p1) continue;
                    p1.sendMessage(MessageColor.NEUTRAL + "[" + MessageColor.POSITIVE + cs.getName() + MessageColor.NEUTRAL + " -> " + MessageColor.POSITIVE + t.getName() + MessageColor.NEUTRAL + "] " + m);
                }
            }
        }
        return true;
    }
}
