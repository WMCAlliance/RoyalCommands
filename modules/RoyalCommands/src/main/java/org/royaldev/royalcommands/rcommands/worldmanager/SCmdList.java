/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.worldmanager;

import java.util.Iterator;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdWorldManager;
import org.royaldev.royalcommands.rcommands.SubCommand;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class SCmdList extends SubCommand<CmdWorldManager> {

    public SCmdList(final RoyalCommands instance, final CmdWorldManager parent) {
        super(instance, parent, "list", true, "Lists all the loaded worlds.", "<command>", new String[0], new Short[0]);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!Config.useWorldManager) {
            cs.sendMessage(MessageColor.NEGATIVE + "WorldManager is disabled!");
            return true;
        }
        Iterator<World> worlds = this.plugin.getServer().getWorlds().iterator();
        cs.sendMessage(MessageColor.POSITIVE + "Worlds:");
        BaseComponent bc = new TextComponent("");
        while (worlds.hasNext()) {
            final World world = worlds.next();
            TextComponent tc = new TextComponent(RUtils.getMVWorldName(world));
            tc.setColor(MessageColor.NEUTRAL.bc());
            Text tt = new Text(MessageColor.POSITIVE + "Click to teleport" + "\nto " + MessageColor.NEUTRAL + RUtils.getMVWorldName(world));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tt));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpw " + world.getName()));
            bc.addExtra(tc);
            if (worlds.hasNext()) bc.addExtra(MessageColor.RESET + ", ");
        }
        cs.spigot().sendMessage(bc);
        return true;
    }
}
