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
public class CmdSetUserdata extends TabCommand {

    public CmdSetUserdata(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 3) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        String name = args[0];
        String node = args[1];
        String value = RoyalCommands.getFinalArg(args, 2);
        OfflinePlayer op = RUtils.getOfflinePlayer(name);
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(op);
        if (!pcm.exists() || !op.hasPlayedBefore()) {
            cs.sendMessage(MessageColor.NEGATIVE + "No such player!");
            return true;
        }
        pcm.set(node, value);
        cs.sendMessage(MessageColor.POSITIVE + "Set " + MessageColor.NEUTRAL + node + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + value + MessageColor.POSITIVE + " for the userdata of " + MessageColor.NEUTRAL + name + MessageColor.POSITIVE + ".");
        return true;
    }
}
