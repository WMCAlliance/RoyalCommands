/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.whitelist;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdWhitelist;
import org.royaldev.royalcommands.rcommands.SubCommand;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

public class SCmdList extends SubCommand<CmdWhitelist> {

    public SCmdList(final RoyalCommands instance, final CmdWhitelist parent) {
        super(instance, parent, "list", true, "List all players in the whitelist.", "<command>", new String[0], new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (this.plugin.whl == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "The whitelist.yml file was invalid! Cannot use whitelist.");
            return true;
        }
        if (Config.whitelist.size() == 0) {
            cs.sendMessage(MessageColor.POSITIVE + "The whitelist is empty.");
            return true;
        }
        final StringBuilder sb = new StringBuilder();
        int unknown = 0;
        for (String player : Config.whitelist) {
            final UUID uuid = UUID.fromString(player);
            final RPlayer rp = MemoryRPlayer.getRPlayer(uuid);
            // forceGetName is incredibly slow. Find an alternative.
            // String name = rp.getName();
            // if (rp.getPlayer() == null && name == null)
            //     name = RUtils.forceGetName(uuid);

            sb.append(MessageColor.NEUTRAL);
            sb.append(rp.getPlayer() != null ? rp.getPlayer().getDisplayName() : uuid);
            sb.append(MessageColor.RESET);
            sb.append(", ");
            if (rp.getPlayer() == null)
                unknown++;
        }
        cs.sendMessage(MessageColor.POSITIVE + "The whitelist contains " + MessageColor.NEUTRAL + Config.whitelist.size() + MessageColor.POSITIVE + " players." + (unknown > 0 ? " " + MessageColor.NEUTRAL + unknown + MessageColor.POSITIVE + " have never played." : ""));
        cs.sendMessage(sb.substring(0, sb.length() - 4));
        return true;
    }
}
