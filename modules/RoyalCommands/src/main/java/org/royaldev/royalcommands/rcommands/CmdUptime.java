/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;

@ReflectCommand
public class CmdUptime extends BaseCommand {

    public CmdUptime(final RoyalCommands instance, final String name) {
        super(instance, name, true);
    }

    @Override
    protected boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
        final long startTime = this.plugin.getStartTime();
        new FancyMessage("The server started ")
            .color(MessageColor.POSITIVE.cc())
            .then(RUtils.formatDateDiff(startTime))
            .color(MessageColor.NEUTRAL.cc())
            .tooltip("Timestamp: " + startTime)
            .then(" ago.")
            .color(MessageColor.POSITIVE.cc())
            .send(cs);
        return true;
    }
}
