/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.vip;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.rcommands.CmdVip;
import org.royaldev.royalcommands.rcommands.SubCommand;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class SCmdAdd extends SubCommand<CmdVip> {

    public SCmdAdd(final RoyalCommands instance, final CmdVip parent) {
        super(instance, parent, "add", true, "Add a player to the VIP list.", "<command> (player)", new String[0], new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (eargs.length < 1) {
            cs.sendMessage(MessageColor.NEGATIVE + "No player specified!");
            return true;
        }
        RPlayer t = MemoryRPlayer.getRPlayer(eargs[0]);
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t.getUUID());
        if (!pcm.exists()) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        if (pcm.get("vip") != null && pcm.getBoolean("vip")) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player is already in the VIP list.");
            return true;
        }

        TextComponent tc = new TextComponent("Added ");
        tc.setColor(MessageColor.POSITIVE.bc());
        BaseComponent bc = new TextComponent(t.getName());
        bc.setColor(MessageColor.NEUTRAL.bc());
        if (t.getPlayer() != null) {
            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RUtils.getPlayerTooltip(t.getPlayer())));
        } else {
            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(t.getUUID().toString())));
        }
        tc.addExtra(bc);

        tc.addExtra(TextComponent.fromLegacy(" " + MessageColor.POSITIVE.bc() + "to the VIP list."));

        pcm.set("vip", true);
        cs.spigot().sendMessage(tc);
        return true;
    }
}
