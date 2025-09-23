/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdFixChunk extends TabCommand {

    public CmdFixChunk(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        Chunk c = p.getLocation().getChunk();
        if (!c.isLoaded()) c.load(true);
        @SuppressWarnings("deprecation")
        boolean worked = p.getWorld().refreshChunk(c.getX(), c.getZ());
        c.unload();
        c.load();
        if (worked) cs.sendMessage(MessageColor.POSITIVE + "The chunk you're standing in has been refreshed!");
        else cs.sendMessage(MessageColor.NEGATIVE + "The chunk could not be refreshed.");
        return true;
    }
}
