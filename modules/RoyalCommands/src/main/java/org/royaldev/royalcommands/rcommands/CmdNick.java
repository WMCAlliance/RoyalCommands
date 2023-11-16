/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.rcommands.TabCommand.CompletionType;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

@ReflectCommand
public class CmdNick extends TabCommand {

    public CmdNick(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort(), CompletionType.LIST.getShort()});
    }

    @Override
	protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>(Arrays.asList("clear"));
    }

    private void clearNick(final RPlayer rp, final CommandSender cs) {
        rp.getNick().clear();
        cs.sendMessage(MessageColor.POSITIVE + "You reset the nickname of " + MessageColor.NEUTRAL + rp.getName() + MessageColor.POSITIVE + ".");
        if (cs instanceof Player && !rp.isSameAs((Player) cs)) {
            rp.sendMessage(MessageColor.POSITIVE + "Your nickname was reset by " + MessageColor.NEUTRAL + cs.getName() + MessageColor.POSITIVE + ".");
        }
    }

    /**
     * Check to see if enough time has passed to allow the nick to be updated again.
     *
     * @param rp Player to check for
     * @return true if nick may be updated, false if not
     */
    private boolean hasTimePassed(final CommandSender cs, final RPlayer rp) {
        if (this.ah.isAuthorized(cs, "rcmds.exempt.nick.changelimit")) return true;
        final long nickChangeLimit = RUtils.timeFormatToSeconds(Config.nickChangeLimit);
        if (nickChangeLimit == -1L) return true;
        final long lastUpdate = rp.getNick().getLastUpdate();
        return lastUpdate == -1L || lastUpdate + (nickChangeLimit * 1000L) < System.currentTimeMillis();
    }

    private boolean isAllowedColor(final CommandSender cs) {
        return Config.nickColorsEnabled && (!Config.nickColorsOnlyWithPerm || this.ah.isAuthorized(cs, "rcmds.nick.colors") || this.ah.isAuthorized(cs, "rcmds.nick.color"));
    }

    private boolean isLengthLegal(final CommandSender cs, final String nick) {
        return this.ah.isAuthorized(cs, "rcmds.exempt.nick.length") || !(Config.nickMinLength != 0 && nick.length() < Config.nickMinLength) && !(Config.nickMaxLength != 0 && nick.length() > Config.nickMaxLength);
    }

    private boolean matchesRegex(final CommandSender cs, final String nick) {
        return !Config.nickRegexEnabled || this.ah.isAuthorized(cs, "rcmds.exempt.nick.regex") || nick.matches(Config.nickRegexPattern);
    }

    private void sendLengthMessage(final CommandSender cs, final String nick) {
        if (Config.nickMinLength != 0 && nick.length() < Config.nickMinLength) {
            cs.sendMessage(MessageColor.NEGATIVE + "Nick must be at least " + MessageColor.NEUTRAL + Config.nickMinLength + MessageColor.NEGATIVE + " characters long.");
        }
        if (Config.nickMaxLength != 0 && nick.length() > Config.nickMaxLength) {
            cs.sendMessage(MessageColor.NEGATIVE + "Nick cannot be longer than " + MessageColor.NEUTRAL + Config.nickMaxLength + MessageColor.NEGATIVE + " characters.");
        }
    }

    private void sendTimeMessage(final CommandSender cs, final RPlayer rp) {
        final long nickChangeLimit = RUtils.timeFormatToSeconds(Config.nickChangeLimit);
        final long lastUpdate = rp.getNick().getLastUpdate();
        cs.sendMessage(MessageColor.NEUTRAL + RUtils.formatDateDiff(lastUpdate + (nickChangeLimit * 1000L)) + MessageColor.NEGATIVE + " must elapse before the nick for " + MessageColor.NEUTRAL + rp.getName() + MessageColor.NEGATIVE + " may be changed again.");
    }

    private void setNick(final CommandSender cs, final RPlayer rp, final String nick) {
        rp.getNick().set(nick);
        cs.sendMessage(MessageColor.POSITIVE + "You have changed the nick of " + MessageColor.NEUTRAL + rp.getName() + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + nick + MessageColor.POSITIVE + ".");
        if (cs instanceof Player && !rp.isSameAs((Player) cs)) {
            rp.sendMessage(MessageColor.POSITIVE + "Your nickname was changed to " + MessageColor.NEUTRAL + nick + MessageColor.POSITIVE + " by " + MessageColor.NEUTRAL + cs.getName() + MessageColor.POSITIVE + ".");
        }
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {

        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }

        final RPlayer rpt = MemoryRPlayer.getRPlayer(args[0]);

        if (rpt != null && args.length < 2) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }

        if (cs instanceof Player) {
            final boolean same = rpt.isSameAs((OfflinePlayer) cs);
            if (!same && !this.ah.isAuthorized(cs, cmd, PermType.OTHERS) || rpt.isOnline() && this.ah.isAuthorized(rpt.getPlayer(), cmd, PermType.EXEMPT)) {
                RUtils.dispNoPerms(cs);
                return true;
            }
        }
        final PlayerConfiguration pcm = rpt.getPlayerConfiguration();
        if (!pcm.exists()) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player doesn't exist!");
            return true;
        }
        String newNick = args[1];
        if (args[1].equalsIgnoreCase("clear")) {
            this.clearNick(rpt, cs);
            return true;
        }
        if (!this.hasTimePassed(cs, rpt)) {
            this.sendTimeMessage(cs, rpt);
            return true;
        }
        if (!this.matchesRegex(cs, newNick)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That nickname contains invalid characters!");
            return true;
        }
        if (!this.isLengthLegal(cs, newNick)) {
            this.sendLengthMessage(cs, newNick);
            return true;
        }
        if (!this.isAllowedColor(cs)) {
            newNick = RUtils.decolorize(newNick);
        }
        newNick = RUtils.colorize(Config.nickPrefix + newNick);
        this.setNick(cs, rpt, newNick);
        return true;
    }

}
