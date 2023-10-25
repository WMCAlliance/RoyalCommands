/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.home.BaseHomeCommand;
import org.royaldev.royalcommands.rcommands.home.Home;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

@ReflectCommand
public class CmdHome extends BaseHomeCommand {

    public CmdHome(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.LIST.getShort()}, false);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Player p, final Command cmd, final String label, final String[] eargs, final CommandArguments ca, final Home home) {
        final RPlayer rp = MemoryRPlayer.getRPlayer(p);
        final String error = rp.getTeleporter().teleport(home.getLocation());
        if (!error.isEmpty()) cs.sendMessage(MessageColor.NEGATIVE + error);
        else {
            cs.sendMessage(MessageColor.POSITIVE + "Teleported to home " + MessageColor.NEUTRAL + home.getName() + MessageColor.POSITIVE + " for " + MessageColor.NEUTRAL + home.getRPlayer().getName() + MessageColor.POSITIVE + ".");
        }
        return true;
    }
}
