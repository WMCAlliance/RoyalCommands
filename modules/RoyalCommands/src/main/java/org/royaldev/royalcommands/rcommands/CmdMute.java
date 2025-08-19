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

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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

        long duration = System.currentTimeMillis() + (muteTime * 1000L);
        BaseComponent timeText = TextComponent.fromLegacy(MessageColor.NEUTRAL + RUtils.formatDateDiff(duration).trim());

        TextComponent tc = new TextComponent();

        TextComponent positive = new TextComponent("You have toggled mute ");
        positive.setColor(MessageColor.POSITIVE.bc());
        tc.addExtra(positive.duplicate());

        TextComponent neutral = new TextComponent(wasMuted ? "off" : "on");
        neutral.setColor(MessageColor.NEUTRAL.bc());
        tc.addExtra(neutral.duplicate());

        positive.setText(" for ");
        tc.addExtra(positive.duplicate());

        neutral.setText(t.getName());
        neutral.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RUtils.getPlayerTooltip(t)));
        tc.addExtra(neutral.duplicate());

        if (muteTime > 0L && !wasMuted) {
            positive.setText(" for ");
            tc.addExtra(positive.duplicate());
            tc.addExtra(timeText);
        }
        if (!reason.isEmpty()) {
            positive.setText(" for ");
            tc.addExtra(positive.duplicate());
            neutral.setText(reason);
            neutral.setHoverEvent((HoverEvent) null);
            tc.addExtra(neutral.duplicate());
        }
        positive.setText(".");
        tc.addExtra(positive.duplicate());
        cs.spigot().sendMessage(tc);
        if (t.isOnline()) {
            TextComponent tc2 = new TextComponent();

            positive.setText("You have been ");
            tc2.addExtra(positive.duplicate());

            neutral.setText(wasMuted ? "unmuted" : "muted");
            neutral.setHoverEvent((HoverEvent) null);
            tc2.addExtra(neutral.duplicate());

            positive.setText(" by ");
            tc2.addExtra(positive.duplicate());

            neutral.setText(cs.getName());
            neutral.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RUtils.getPlayerTooltip(cs)));
            tc2.addExtra(neutral.duplicate());


            if (muteTime > 0L && !wasMuted) {
                positive.setText(" for ");
                tc2.addExtra(positive.duplicate());
                tc2.addExtra(timeText);
            }
            if (!reason.isEmpty()) {
                positive.setText(" for ");
                tc2.addExtra(positive.duplicate());
                neutral.setText(reason);
                neutral.setHoverEvent((HoverEvent) null);
                tc2.addExtra(neutral.duplicate());
            }
            positive.setText(".");
            tc2.addExtra(positive.duplicate());
            t.getPlayer().spigot().sendMessage(tc2);
        }
        return true;
    }
}
