/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

@ReflectCommand
public class CmdUptime extends BaseCommand {

    public CmdUptime(final RoyalCommands instance, final String name) {
        super(instance, name, true);
    }

    @Override
    protected boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
        final long startTime = this.plugin.getStartTime();
        BaseComponent datetime = TextComponent.fromLegacy(RUtils.formatDateDiff(startTime), MessageColor.NEUTRAL.bc());
        datetime.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Timestamp: " + startTime)));
        BaseComponent[] message = new ComponentBuilder("The server started ")
                .color(MessageColor.POSITIVE.bc())
                .append(datetime)
                .color(MessageColor.NEUTRAL.bc())
                .append("ago.")
                .color(MessageColor.POSITIVE.bc())
                .event((HoverEvent)null)
                .create();
        cs.spigot().sendMessage(message);
        return true;
    }
}
