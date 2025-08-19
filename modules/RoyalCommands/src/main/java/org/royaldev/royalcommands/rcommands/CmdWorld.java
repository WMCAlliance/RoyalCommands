/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.Iterator;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

@ReflectCommand
public class CmdWorld extends TabCommand {

    public CmdWorld(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.WORLD.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        World w = args.length > 0 ? this.plugin.getServer().getWorld(args[0]) : null;
        if (args.length < 1 || w == null) {
            if (args.length > 0) {
                cs.sendMessage(MessageColor.NEGATIVE + "That world does not exist!");
            }
            Iterator<World> worlds = this.plugin.getServer().getWorlds().iterator();
            cs.sendMessage(MessageColor.POSITIVE + "Worlds:");
            BaseComponent bc = new TextComponent("");
            while (worlds.hasNext()) {
                final World world = worlds.next();
                if (Config.hiddenWorlds.contains(world.getName())) continue;
                TextComponent tc = new TextComponent(RUtils.getMVWorldName(world));
                tc.setColor(MessageColor.NEUTRAL.bc());
                Text tt = new Text(MessageColor.POSITIVE + "Click to teleport" + "\nto " + MessageColor.NEUTRAL + RUtils.getMVWorldName(world));
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tt));
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpw " + world.getName()));
                bc.addExtra(tc);
                if (worlds.hasNext()) bc.addExtra(MessageColor.RESET + ", "); // it's not a color OR a style
            }
            cs.spigot().sendMessage(bc);
            return true;
        }
        if (Config.hiddenWorlds.contains(w.getName())) return true;
        Player p = (Player) cs;
        p.sendMessage(MessageColor.POSITIVE + "Teleporting you to world " + MessageColor.NEUTRAL + RUtils.getMVWorldName(w) + MessageColor.POSITIVE + ".");
        final RPlayer rp = MemoryRPlayer.getRPlayer(p);
        String error = rp.getTeleporter().teleport(CmdSpawn.getWorldSpawn(w));
        if (!error.isEmpty()) {
            p.sendMessage(MessageColor.NEGATIVE + error);
            return true;
        }
        return true;
    }
}
