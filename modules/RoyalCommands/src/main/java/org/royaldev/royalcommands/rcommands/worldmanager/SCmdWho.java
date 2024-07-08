/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.worldmanager;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdWorldManager;
import org.royaldev.royalcommands.rcommands.SubCommand;

public class SCmdWho extends SubCommand<CmdWorldManager> {

    public SCmdWho(final RoyalCommands instance, final CmdWorldManager parent) {
        super(instance, parent, "who", true, "Displays who is in all loaded worlds.", "<command>", new String[0], new Short[0]);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!Config.useWorldManager) {
            cs.sendMessage(MessageColor.NEGATIVE + "WorldManager is disabled!");
            return true;
        }
        for (final World w : this.plugin.getServer().getWorlds()) {
            final StringBuilder sb = new StringBuilder(RUtils.getMVWorldName(w));
            sb.append(": ");
            if (w.getPlayers().isEmpty()) {
                if (Config.wmShowEmptyWorlds) {
                    cs.sendMessage(MessageColor.NEGATIVE + "No players in " + MessageColor.NEUTRAL + RUtils.getMVWorldName(w) + MessageColor.NEGATIVE + ".");
                }
                continue;
            }
            for (final Player p : w.getPlayers()) {
                sb.append(MessageColor.NEUTRAL);
                sb.append(p.getDisplayName());
                sb.append(MessageColor.RESET);
                sb.append(", ");
            }
            cs.sendMessage(sb.substring(0, sb.length() - 4));
        }
        return true;
    }
}
