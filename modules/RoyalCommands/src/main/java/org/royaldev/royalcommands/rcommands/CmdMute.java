/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;

@ReflectCommand
public class CmdMute extends CACommand {

    public CmdMute(final RoyalCommands instance, final String name) {
        super(instance, name, true);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (eargs.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final OfflinePlayer t = RUtils.getOfflinePlayer(eargs[0]);
        final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
        if (!pcm.exists()) {
            if (!t.isOnline() && !t.hasPlayedBefore()) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            pcm.createFile();
        }
        if (cs.getName().equalsIgnoreCase(t.getName())) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't mute yourself!");
            return true;
        }
        if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't mute that player!");
            return true;
        }
        final boolean wasMuted = pcm.getBoolean("muted");
        long muteTime = 0L;
        if (eargs.length > 1) muteTime = (long) RUtils.timeFormatToSeconds(eargs[1]);
        if (muteTime < 0L) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid time format!");
            return true;
        }
        final String reason = eargs.length > 2 ? RUtils.colorize(RoyalCommands.getFinalArg(eargs, 2)) : "";
        pcm.set("muted", !wasMuted);
        if (muteTime > 0L && !wasMuted) pcm.set("mutetime", muteTime);
        else if (wasMuted) pcm.set("mutetime", null);
        pcm.set("mutedat", System.currentTimeMillis());
        FancyMessage fm = new FancyMessage("You have toggled mute ").color(MessageColor.POSITIVE.cc()).then(wasMuted ? "off" : "on").color(MessageColor.NEUTRAL.cc()).then(" for ").color(MessageColor.POSITIVE.cc()).then(t.getName()).color(MessageColor.NEUTRAL.cc()).formattedTooltip(RUtils.getPlayerTooltip(t));
        if (muteTime > 0L && !wasMuted) {
            fm.then(" for ").color(MessageColor.POSITIVE.cc()).then(RUtils.formatDateDiff((muteTime * 1000L) + System.currentTimeMillis()).substring(1)).color(MessageColor.NEUTRAL.cc());
        }
        if (!reason.isEmpty()) {
            fm.then(" for ").color(MessageColor.POSITIVE.cc()).then(reason).color(MessageColor.NEUTRAL.cc());
        }
        fm.then(".").color(MessageColor.POSITIVE.cc());
        fm.send(cs);
        if (t.isOnline()) {
            fm = new FancyMessage("You have been ").color(MessageColor.POSITIVE.cc()).then(wasMuted ? "unmuted" : "muted").color(MessageColor.NEUTRAL.cc()).then(" by ").color(MessageColor.POSITIVE.cc()).then(cs.getName()).color(MessageColor.NEUTRAL.cc()).formattedTooltip(RUtils.getPlayerTooltip(cs));
            if (muteTime > 0L && !wasMuted) {
                fm.then(" for ").color(MessageColor.POSITIVE.cc()).then(RUtils.formatDateDiff((muteTime * 1000L) + System.currentTimeMillis()).substring(1)).color(MessageColor.NEUTRAL.cc());
            }
            if (!reason.isEmpty()) {
                fm.then(" for ").color(MessageColor.POSITIVE.cc()).then(reason).color(MessageColor.NEUTRAL.cc());
            }
            fm.then(".").color(MessageColor.POSITIVE.cc());
            fm.send(t.getPlayer());
        }
        return true;
    }
}
