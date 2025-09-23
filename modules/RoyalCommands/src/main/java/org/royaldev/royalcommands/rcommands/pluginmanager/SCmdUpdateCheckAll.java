/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.pluginmanager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdPluginManager;
import org.royaldev.royalcommands.rcommands.SubCommand;

public class SCmdUpdateCheckAll extends SubCommand<CmdPluginManager> {

    public SCmdUpdateCheckAll(final RoyalCommands instance, final CmdPluginManager parent) {
        super(instance, parent, "updatecheckall", true, "Attempts to check for newest version of all plugins", "<command>", new String[0], new Short[0]);
    }

    @Override
    public boolean runCommand(final CommandSender cs, Command cmd, String label, String[] eargs, CommandArguments ca) {
        final PluginManager pm = this.plugin.getServer().getPluginManager();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                for (Plugin p : pm.getPlugins()) {
                    String version = p.getDescription().getVersion();
                    if (version == null) continue;
                    String checked;
                    try {
                        checked = SCmdUpdateCheckAll.this.getParent().updateCheck(p.getName(), version);
                    } catch (Exception e) {
                        continue;
                    }
                    if (checked.contains(version)) continue;
                    cs.sendMessage(MessageColor.NEUTRAL + p.getName() + MessageColor.POSITIVE + " may have an update. C: " + MessageColor.NEUTRAL + version + MessageColor.POSITIVE + " N: " + MessageColor.NEUTRAL + checked);
                }
                cs.sendMessage(MessageColor.POSITIVE + "Finished checking for updates.");
            }
        };
        cs.sendMessage(MessageColor.POSITIVE + "Starting check for updates. This may take a while..");
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, r);
        return true;
    }
}
