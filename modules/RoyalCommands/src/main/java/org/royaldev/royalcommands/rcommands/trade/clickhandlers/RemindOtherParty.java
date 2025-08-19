/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.trade.clickhandlers;

import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.gui.inventory.ClickHandler;
import org.royaldev.royalcommands.rcommands.trade.Party;
import org.royaldev.royalcommands.rcommands.trade.Trade;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class RemindOtherParty implements ClickHandler {

    private final Trade trade;

    public RemindOtherParty(final Trade trade) {
        this.trade = trade;
    }

    @Override
    public boolean onClick(final org.royaldev.royalcommands.gui.inventory.ClickEvent clickEvent) {
        final Party party = this.trade.get(clickEvent.getClicker().getUniqueId());
        if (party == null) return false;
        final Party remind = party.getOther();
        final RPlayer rp = MemoryRPlayer.getRPlayer(this.trade.get(remind));
        final Player remindPlayer = rp.getPlayer();
        if (remindPlayer == null) return false;
        String clicker = clickEvent.getClicker().getName();
        remindPlayer.sendMessage(MessageColor.NEUTRAL + clicker + MessageColor.POSITIVE + " would like you to check your mutual trade.");

        TextComponent command = new TextComponent("here");
        command.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade " + clicker));
        command.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Open trade window")));
        command.setColor(MessageColor.POSITIVE.bc());
        BaseComponent[] component = new ComponentBuilder("To do so, click ")
                .color(MessageColor.POSITIVE.bc())
                .append(command)
                .color(MessageColor.NEUTRAL.bc())
                .create();
        remindPlayer.spigot().sendMessage(component);
        return false;
    }
}
