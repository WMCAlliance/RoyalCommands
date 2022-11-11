/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

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
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

@ReflectCommand
public class CmdNick extends CACommand {

    private static final Flag CLEAR_FLAG = new Flag("clear", "c", "remove", "r", "disable", "d", "off", "o");
    private static final Flag<String> TARGET_FLAG = new Flag<>(String.class, "target", "t", "player", "p");
    private static final Flag<String> NICK_FLAG = new Flag<>(String.class, "nickname", "nick", "n");

    public CmdNick(final RoyalCommands instance, final String name) {
        super(instance, name, true);
        this.addExpectedFlag(CmdNick.TARGET_FLAG);
        this.addExpectedFlag(CmdNick.CLEAR_FLAG);
        this.addExpectedFlag(CmdNick.NICK_FLAG);
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
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!ca.hasContentFlag(CmdNick.TARGET_FLAG)) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final RPlayer rpt = MemoryRPlayer.getRPlayer(ca.getFlag(CmdNick.TARGET_FLAG).getValue());
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
        if (ca.hasFlag(CmdNick.CLEAR_FLAG)) {
            this.clearNick(rpt, cs);
            return true;
        }
        if (!this.hasTimePassed(cs, rpt)) {
            this.sendTimeMessage(cs, rpt);
            return true;
        }
        if (!ca.hasContentFlag(CmdNick.NICK_FLAG)) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        String newNick = ca.getFlag(CmdNick.NICK_FLAG).getValue();
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
