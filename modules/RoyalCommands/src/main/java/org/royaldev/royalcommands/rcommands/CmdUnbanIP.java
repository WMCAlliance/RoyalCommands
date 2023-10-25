/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdUnbanIP extends TabCommand {

    public CmdUnbanIP(final RoyalCommands instance, final String name) {
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
        OfflinePlayer op = this.plugin.getServer().getOfflinePlayer(args[0]);
        String ip = (!op.hasPlayedBefore()) ? args[0] : PlayerConfigurationManager.getConfiguration(op).getString("ip");
        if (ip == null) ip = args[0];
        if (!isValid(ip)) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid IP (" + MessageColor.NEUTRAL + ip + MessageColor.NEGATIVE + ").");
            return true;
        }
        try {
            InetAddress addr = InetAddress.getByName(ip);
            this.plugin.getServer().unbanIP(addr);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        if (!op.hasPlayedBefore()) {
            cs.sendMessage(MessageColor.POSITIVE + "Unbanned IP " + MessageColor.NEUTRAL + ip + MessageColor.POSITIVE + ".");
            return true;
        } else {
            RUtils.unbanPlayer(op);
            cs.sendMessage(MessageColor.POSITIVE + "Unbanned IP of " + MessageColor.NEUTRAL + op.getName() + MessageColor.POSITIVE + " (" + MessageColor.NEUTRAL + ip + MessageColor.POSITIVE + ").");
            return true;
        }
    }
}
