/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.worldmanager;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdWorldManager;
import org.royaldev.royalcommands.rcommands.SubCommand;

import java.io.File;

public class SCmdLoad extends SubCommand<CmdWorldManager> {

    public SCmdLoad(final RoyalCommands instance, final CmdWorldManager parent) {
        super(instance, parent, "load", true, "Loads a world.", "<command> [name]", new String[0], new Short[0]);
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
        final String name = args[0];
        boolean contains = false;
        File[] fs = this.plugin.getServer().getWorldContainer().listFiles();
        if (fs == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "The world directory is invalid!");
            return true;
        }
        for (File f : fs) if (f.getName().equals(name)) contains = true;
        if (!contains) {
            cs.sendMessage(MessageColor.NEGATIVE + "No such world!");
            return true;
        }
        World w;
        try {
            w = RoyalCommands.wm.loadWorld(name);
        } catch (IllegalArgumentException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "No such world!");
            return true;
        } catch (NullPointerException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Could not read world folders!");
            return true;
        }
        cs.sendMessage(MessageColor.POSITIVE + "Loaded world " + MessageColor.NEUTRAL + w.getName() + MessageColor.POSITIVE + ".");
        return true;
    }
}
