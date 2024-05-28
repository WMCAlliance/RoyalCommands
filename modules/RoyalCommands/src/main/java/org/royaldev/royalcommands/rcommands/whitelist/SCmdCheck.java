/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.whitelist;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdWhitelist;
import org.royaldev.royalcommands.rcommands.SubCommand;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

public class SCmdCheck extends SubCommand<CmdWhitelist> {

    public SCmdCheck(final RoyalCommands instance, final CmdWhitelist parent) {
        super(instance, parent, "check", true, "Checks if a player is in the whitelist.", "<command> (player)", new String[0], new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (this.plugin.whl == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "The whitelist.yml file was invalid! Cannot use whitelist.");
            return true;
        }
        if (eargs.length < 1){
            return false;
        }
        final RPlayer rp = this.getParent().getRPlayer(RoyalCommands.getFinalArg(eargs, 0));
        final String uuid = rp.getUUID().toString();
        cs.sendMessage(
            Config.whitelist.contains(uuid)
                ? MessageColor.NEUTRAL + rp.getName() + MessageColor.POSITIVE + " (" + MessageColor.NEUTRAL + uuid + MessageColor.POSITIVE + ") is in the whitelist."
                : MessageColor.NEUTRAL + rp.getName() + MessageColor.NEGATIVE + " (" + MessageColor.NEUTRAL + uuid + MessageColor.POSITIVE + ") is not in the whitelist."
        );
        return true;
    }
}
