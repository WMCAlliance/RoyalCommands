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
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdWorldManager;
import org.royaldev.royalcommands.rcommands.SubCommand;
import org.royaldev.royalcommands.rcommands.TabCommand;

public class SCmdUnload extends SubCommand<CmdWorldManager> {

    public SCmdUnload(final RoyalCommands instance, final CmdWorldManager parent) {
        super(instance, parent, "unload", true, "Unloads a world. If eject is specified, will kick all players on the world.", "<command> [name] (eject)", new String[0], new Short[]{TabCommand.CompletionType.WORLD.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, final CommandArguments ca) {
        if (!Config.useWorldManager) {
            cs.sendMessage(MessageColor.NEGATIVE + "WorldManager is disabled!");
            return true;
        }
        if (args.length < 1) {
            cs.sendMessage(MessageColor.NEGATIVE + "Not enough arguments! Try " + MessageColor.NEUTRAL + "/" + label + MessageColor.NEGATIVE + " for help.");
            return true;
        }
        final boolean eject = args.length > 1;
        final World w = this.plugin.getServer().getWorld(args[0]);
        if (w == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "No such world!");
            return true;
        }
        cs.sendMessage(MessageColor.POSITIVE + "Unloading world...");
        if (eject) for (Player p : w.getPlayers()) p.kickPlayer("Your world is being unloaded!");
        final boolean success = this.plugin.getServer().unloadWorld(w, true);
        if (success) cs.sendMessage(MessageColor.POSITIVE + "World unloaded successfully!");
        else cs.sendMessage(MessageColor.NEGATIVE + "Could not unload that world.");
        return true;
    }
}
