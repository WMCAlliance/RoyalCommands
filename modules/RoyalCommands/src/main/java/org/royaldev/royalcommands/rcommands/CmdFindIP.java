/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdFindIP extends TabCommand {

    public CmdFindIP(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    private boolean isValid(String address) {
        if (address == null) return false;
        String[] ips = address.split("\\.");
        if (ips.length != 4) return false;
        for (String s : ips) {
            int ip;
            try {
                ip = Integer.valueOf(s);
            } catch (Exception e) {
                return false;
            }
            if (ip > 255) return false;
        }
        return true;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final String ip = args[0].replaceAll("[^\\.\\d]+", "");
        final boolean partial = !isValid(ip);
        if (partial) cs.sendMessage(MessageColor.POSITIVE + "Checking for a partial IP address.");
        final DecimalFormat df = new DecimalFormat("00.00");
        final OfflinePlayer[] offlinePlayers = this.plugin.getServer().getOfflinePlayers();
        if (offlinePlayers.length < 1) {
            cs.sendMessage(MessageColor.NEGATIVE + "No players have played to check IPs for!");
            return true;
        }
        final List<String> hasIP = new ArrayList<>();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                int runThrough = 0;
                for (OfflinePlayer op : offlinePlayers) {
                    runThrough++;
                    int modulo = (int) Math.floor(offlinePlayers.length * (Config.findIpPercent / 100D));
                    if (modulo < 1) modulo = 1;
                    if (runThrough % modulo == 0) {
                        double percentage = ((double) runThrough / (double) offlinePlayers.length) * 100D;
                        cs.sendMessage(MessageColor.POSITIVE + "Searching. " + MessageColor.NEUTRAL + df.format(percentage) + "%" + MessageColor.POSITIVE + " (" + MessageColor.NEUTRAL + runThrough + MessageColor.POSITIVE + "/" + MessageColor.NEUTRAL + offlinePlayers.length + MessageColor.POSITIVE + ") complete.");
                    }
                    boolean alreadyPresent = PlayerConfigurationManager.isConfigurationCreated(op);
                    PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(op);
                    String pip = pcm.getString("ip");
                    if (pip == null) continue;
                    if (pip.contains(ip)) {
                        synchronized (hasIP) {
                            String name = "";
                            try {
                                name = op.getName();
                            } catch (Exception e) {
                                name = op.getUniqueId().toString();
                            }
                            hasIP.add(pcm.getString("name", name) + ((partial) ? MessageColor.POSITIVE + ": " + MessageColor.NEUTRAL + pip : ""));
                        }
                    }
                    if (!alreadyPresent) pcm.discard();
                }
                if (hasIP.isEmpty())
                    cs.sendMessage(MessageColor.NEGATIVE + "No players with that IP address were found.");
                else {
                    cs.sendMessage(MessageColor.POSITIVE + "The following players matched " + MessageColor.NEUTRAL + ip + MessageColor.POSITIVE + ":");
                    for (String name : hasIP) cs.sendMessage("  " + MessageColor.NEUTRAL + name);
                }
            }
        };
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, r);
        return true;
    }
}
