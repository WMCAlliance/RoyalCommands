/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;

@ReflectCommand
public class CmdPlayerSearch extends TabCommand {

    public CmdPlayerSearch(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final String search = RoyalCommands.getFinalArg(args, 0);
        final OfflinePlayer[] ops = this.plugin.getServer().getOfflinePlayers();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                int found = 0;
                for (final OfflinePlayer op : ops) {
                    if (op == null || op.getName() == null) continue;
                    if (!op.getName().toLowerCase().contains(search.toLowerCase())) continue;
                    PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(op);
                    if (!pcm.exists()) continue;
                    long seen = pcm.getLong("seen");
                    if (seen < 1L) continue;
                    found++;
                    String opName = op.getName();
                    String lastseen = (op.isOnline()) ? " now"
                            : RUtils.formatDateDiff(seen) + MessageColor.POSITIVE + " ago";
                    FancyMessage fm = new FancyMessage(String.valueOf(found))
                        .color(MessageColor.POSITIVE.cc())
                        .then(". ")
                        .color(MessageColor.POSITIVE.cc())
                        .then(opName)
                        .formattedTooltip(RUtils.getPlayerTooltip(cs))
                        .command("/whois " + opName)
                        .color(MessageColor.NEUTRAL.cc())
                        .then(" - Last seen")
                        .color(MessageColor.POSITIVE.cc())
                        .then(lastseen)
                        .color(MessageColor.NEUTRAL.cc())
                        .then(".")
                        .color(MessageColor.POSITIVE.cc());
                    fm.send(cs);
                }
                FancyMessage fmR = new FancyMessage("Search completed. ")
                        .color(MessageColor.POSITIVE.cc())
                        .then(String.valueOf(found))
                        .color(MessageColor.NEUTRAL.cc())
                        .then(" results found.")
                        .color(MessageColor.POSITIVE.cc());
                fmR.send(cs);
            }
        };
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, r);
        return true;
    }
}
