/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

// TODO: Just fuck. Make like kickhistory.

@ReflectCommand
public class CmdBanHistory extends TabCommand {

    public CmdBanHistory(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
		/*
			TODO: We could possibly try returning numbers based on the number of bans a user has, but that'd require fetching the player data for autocomplete
			The alternative is to just leave it as-is, empty
		*/
		return new ArrayList<>();
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final RPlayer rp = MemoryRPlayer.getRPlayer(args[0]);
        final OfflinePlayer op = rp.getOfflinePlayer();
        final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(op);
        if (!pcm.exists()) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player has never played before!");
            return true;
        }
        List<String> prevBans = pcm.getStringList("prevbans");
        if (prevBans == null) prevBans = new ArrayList<>();
        if (args.length < 2) {
            if (prevBans.size() < 1) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player has no previous bans.");
                return true;
            }
            cs.sendMessage(MessageColor.NEUTRAL + op.getName() + MessageColor.POSITIVE + " has " + MessageColor.NEUTRAL + prevBans.size() + MessageColor.POSITIVE + " bans.");
            cs.sendMessage(MessageColor.POSITIVE + "Please do " + MessageColor.NEUTRAL + "/" + label + " " + op.getName() + " #" + MessageColor.POSITIVE + " to view a specific ban.");
            return true;
        }
        int number;
        try {
            number = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "The ban selected must be a number!");
            return true;
        }
        number -= 1; // lists 'n stuff;
        String ban;
        try {
            ban = prevBans.get(number);
        } catch (IndexOutOfBoundsException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid ban number!");
            return true;
        }
        // banner,banreason,bannedat,istempban
        String[] baninfo = ban.split("\\u00b5");
        cs.sendMessage(MessageColor.POSITIVE + "Ban log for ban " + MessageColor.NEUTRAL + (number + 1) + MessageColor.POSITIVE + " of " + MessageColor.NEUTRAL + prevBans.size() + MessageColor.POSITIVE + " for " + MessageColor.NEUTRAL + op.getName() + MessageColor.POSITIVE + ".");
        String banner = baninfo[0];
        if (banner.equals("null")) banner = "Unknown";
        cs.sendMessage(MessageColor.POSITIVE + "Banned by " + MessageColor.NEUTRAL + banner);
        String banReason = baninfo[1];
        if (banReason.equals("null")) banReason = "Unknown";
        cs.sendMessage(MessageColor.POSITIVE + "Banned for " + MessageColor.NEUTRAL + banReason);
        String banDateString = baninfo[2];
        if (banDateString.equals("null")) banDateString = "-1";
        long banDate;
        try {
            banDate = Long.parseLong(banDateString);
        } catch (NumberFormatException e) {
            banDate = -1L;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, y hh:mm:ss a");
        String bannedAt = (banDate < 0L) ? "Unknown" : sdf.format(new Date(banDate));
        cs.sendMessage(MessageColor.POSITIVE + "Banned at " + MessageColor.NEUTRAL + bannedAt);
        boolean isTempBan = baninfo[3].equalsIgnoreCase("true");
        cs.sendMessage(MessageColor.POSITIVE + "Was tempban? " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(isTempBan));
        return true;
    }
}
