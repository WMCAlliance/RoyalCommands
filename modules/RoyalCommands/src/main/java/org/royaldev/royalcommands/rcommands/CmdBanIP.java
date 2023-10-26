/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdBanIP extends TabCommand {

    public CmdBanIP(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{TabCommand.CompletionType.LIST.getShort()});
    }
	
    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
		List<String> playerList = new ArrayList<>();
		// List<String> ipAddresses = new ArrayList<>();
		
		for (Player player : cs.getServer().getOnlinePlayers()) {
            // if (!player.getAddress().getHostName().startsWith(arg)) continue;
			playerList.add(player.getName());
			// ipAddresses.add(player.getAddress().getHostName());
		}
		// playerList.addAll(ipAddresses);
		return playerList;
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
        OfflinePlayer op = RUtils.getOfflinePlayer(args[0]);
        String ip = (!op.hasPlayedBefore()) ? args[0] : PlayerConfigurationManager.getConfiguration(op).getString("ip");
        String banreason = (args.length > 1) ? RoyalCommands.getFinalArg(args, 1) : Config.banMessage;
        banreason = RUtils.colorize(banreason);
        if (ip == null) ip = args[0];
        if (!this.isValid(ip)) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid IP (" + MessageColor.NEUTRAL + ip + MessageColor.NEGATIVE + ").");
            return true;
        }
        RUtils.banIP(ip, cs, banreason);
        if (!op.hasPlayedBefore()) {
            cs.sendMessage(MessageColor.POSITIVE + "Banned IP " + MessageColor.NEUTRAL + ip + MessageColor.POSITIVE + ".");
            return true;
        } else {
                /*
                op.setBanned(true);
                RUtils.writeBanHistory(op);
                -- Do not set the player to banned. May revert this. --
                */
            cs.sendMessage(MessageColor.POSITIVE + "Banned IP of " + MessageColor.NEUTRAL + op.getName() + MessageColor.POSITIVE + " (" + MessageColor.NEUTRAL + ip + MessageColor.POSITIVE + ").");
            return true;
        }
    }
}
