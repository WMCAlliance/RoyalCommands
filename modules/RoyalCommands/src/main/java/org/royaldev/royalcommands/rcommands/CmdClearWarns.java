/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdClearWarns extends TabCommand {

    public CmdClearWarns(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        OfflinePlayer op = RUtils.getOfflinePlayer(args[0]);
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(op);
        if (!pcm.exists()) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        if (pcm.get("warns") == null || pcm.getStringList("warns").isEmpty()) {
            cs.sendMessage(MessageColor.NEGATIVE + "There are no warnings for " + MessageColor.NEUTRAL + op.getName() + MessageColor.NEGATIVE + "!");
            return true;
        }
        pcm.set("warns", null);
        cs.sendMessage(MessageColor.POSITIVE + "You've cleared the warnings of " + MessageColor.NEUTRAL + op.getName() + MessageColor.POSITIVE + ".");
        return true;
    }
}
