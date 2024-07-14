/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.worldmanager;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdWorldManager;
import org.royaldev.royalcommands.rcommands.SubCommand;

public class SCmdInfo extends SubCommand<CmdWorldManager> {

    public SCmdInfo(final RoyalCommands instance, final CmdWorldManager parent) {
        super(instance, parent, "info", true, "Displays available world types and environments; if you are a player, displays information about your world.", "<command>", new String[0], new Short[0]);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!Config.useWorldManager) {
            cs.sendMessage(MessageColor.NEGATIVE + "WorldManager is disabled!");
            return true;
        }
        cs.sendMessage(MessageColor.POSITIVE + "RoyalCommands WorldManager Info");
        cs.sendMessage(MessageColor.POSITIVE + "===============================");
        cs.sendMessage(MessageColor.POSITIVE + "Available world types:");
        final StringBuilder types = new StringBuilder("  ");
        for (WorldType t : WorldType.values()) {
            types.append(MessageColor.NEUTRAL).append(t.getName()).append(MessageColor.RESET).append(", ");
        }
        cs.sendMessage(types.substring(0, types.length() - 4));
        cs.sendMessage(MessageColor.POSITIVE + "Available world environments:");
        final StringBuilder envs = new StringBuilder("  ");
        for (Environment e : Environment.values()) {
            envs.append(MessageColor.NEUTRAL).append(e.name()).append(MessageColor.RESET).append(", ");
        }
        cs.sendMessage(envs.substring(0, envs.length() - 4));
        if (!(cs instanceof Player)) return true;
        final Player p = (Player) cs;
        final World w = p.getWorld();
        cs.sendMessage(MessageColor.POSITIVE + "Information on this world:");
        cs.sendMessage(MessageColor.POSITIVE + "  Name: " + MessageColor.NEUTRAL + w.getName());
        cs.sendMessage(MessageColor.POSITIVE + "  Display Name: " + MessageColor.NEUTRAL + RUtils.getMVWorldName(w));
        cs.sendMessage(MessageColor.POSITIVE + "  Environment: " + MessageColor.NEUTRAL + w.getEnvironment().name());
        return true;
    }
}
