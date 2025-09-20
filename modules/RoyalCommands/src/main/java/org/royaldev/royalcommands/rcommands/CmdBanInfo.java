/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdBanInfo extends TabCommand {

    public CmdBanInfo(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final OfflinePlayer op = RUtils.getOfflinePlayer(args[0]);
        final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(op);
        if (!pcm.exists()) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player has never played before!");
            return true;
        }
        if (!op.isBanned()) {
            cs.sendMessage(MessageColor.NEUTRAL + op.getName() + MessageColor.NEGATIVE + " is not banned!");
            return true;
        }
        cs.sendMessage(MessageColor.NEUTRAL + op.getName() + MessageColor.POSITIVE + " is banned!");
        String banner = pcm.getString("banner", "Unknown");
        cs.sendMessage(MessageColor.POSITIVE + "Banned by " + MessageColor.NEUTRAL + banner);
        String banReason = pcm.getString("banreason", "Unknown");
        cs.sendMessage(MessageColor.POSITIVE + "Banned for " + MessageColor.NEUTRAL + banReason);
        long banDate = pcm.getLong("bannedat", -1L);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, y hh:mm:ss a");
        String bannedAt = (banDate < 0L) ? "Unknown" : sdf.format(new Date(banDate));
        cs.sendMessage(MessageColor.POSITIVE + "Banned at " + MessageColor.NEUTRAL + bannedAt);
        boolean isTempBan = pcm.get("bantime") != null;
        cs.sendMessage(MessageColor.POSITIVE + "Is tempban? " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(isTempBan));
        if (!isTempBan) return true;
        String expire = sdf.format(new Date(pcm.getLong("bantime")));
        cs.sendMessage(MessageColor.POSITIVE + "Tempban expires on " + MessageColor.NEUTRAL + expire);
        return true;
    }
}
