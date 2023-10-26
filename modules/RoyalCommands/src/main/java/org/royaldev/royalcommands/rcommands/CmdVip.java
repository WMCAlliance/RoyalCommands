/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdVip extends TabCommand {

    public CmdVip(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.LIST.getShort(), CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>(Arrays.asList("add", "remove", "check", "help"));
    }
	
    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        String command = args[0];
        if (command.equalsIgnoreCase("add")) {
            if (args.length < 2) {
                cs.sendMessage(MessageColor.NEGATIVE + "No player specified!");
                return true;
            }
            OfflinePlayer t = RUtils.getOfflinePlayer(args[1]);
            PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
            if (!pcm.exists()) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            if (pcm.get("vip") != null && pcm.getBoolean("vip")) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player is already in the VIP list.");
                return true;
            }
            pcm.set("vip", true);
            cs.sendMessage(MessageColor.POSITIVE + "Successfully added " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + " to the VIP list.");
            return true;
        } else if (command.equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                cs.sendMessage(MessageColor.NEGATIVE + "No player specified!");
                return true;
            }
            OfflinePlayer t = RUtils.getOfflinePlayer(args[1]);
            PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
            if (!pcm.exists()) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            if (pcm.get("vip") == null || !pcm.getBoolean("vip")) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player is not in the VIP list.");
                return true;
            }
            pcm.set("vip", false);
            cs.sendMessage(MessageColor.POSITIVE + "Successfully removed " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + " to the VIP list.");
            return true;
        } else if (command.equalsIgnoreCase("check")) {
            if (args.length < 2) {
                cs.sendMessage(MessageColor.NEGATIVE + "No player specified!");
                return true;
            }
            OfflinePlayer t = RUtils.getOfflinePlayer(args[1]);
            PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
            if (!pcm.exists()) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            boolean inList = pcm.getBoolean("vip");
            if (inList) {
                cs.sendMessage(MessageColor.POSITIVE + "The player " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + " is in the VIP list.");
                return true;
            }
            cs.sendMessage(MessageColor.NEGATIVE + "The player " + MessageColor.NEUTRAL + t.getName() + MessageColor.NEGATIVE + " is not in the VIP list.");
            return true;
        } else if (command.equalsIgnoreCase("help") || command.equalsIgnoreCase("?")) {
            String cmdName = cmd.getName();
            cs.sendMessage(MessageColor.NEUTRAL + "/" + cmdName + " add [player] " + MessageColor.POSITIVE + " - Adds a player to the VIP list.");
            cs.sendMessage(MessageColor.NEUTRAL + "/" + cmdName + " remove [player] " + MessageColor.POSITIVE + " - Removes a player from the VIP list.");
            cs.sendMessage(MessageColor.NEUTRAL + "/" + cmdName + " check [player] " + MessageColor.POSITIVE + " - Checks if a player is in the VIP list.");
            cs.sendMessage(MessageColor.NEUTRAL + "/" + cmdName + " help " + MessageColor.POSITIVE + " - Displays this help.");
            return true;
        } else {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid subcommand!");
            cs.sendMessage(MessageColor.NEGATIVE + "Try " + MessageColor.NEUTRAL + "/" + cmd.getName() + " ?" + MessageColor.NEGATIVE + ".");
            return true;
        }
    }
}
