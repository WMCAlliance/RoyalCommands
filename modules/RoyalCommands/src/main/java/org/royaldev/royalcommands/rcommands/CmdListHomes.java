/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.home.Home;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

@ReflectCommand
public class CmdListHomes extends TabCommand {

    public CmdListHomes(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player) && args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final OfflinePlayer t;
        if (args.length < 1) t = (OfflinePlayer) cs;
        else {
            if (!this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You cannot list other players' homes!");
                return true;
            }
            t = RUtils.getOfflinePlayer(args[0]);
            if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You can't list that player's homes!");
                return true;
            }
        }
        final RPlayer rp = MemoryRPlayer.getRPlayer(t);
        final Map<World, List<Home>> sortedHomes = new HashMap<>();
        for (final Home h : rp.getHomes()) {
            if (h == null) continue;
            final Location l = h.getLocation();
            if (l == null) continue;
            final World w = l.getWorld();
            final List<Home> homes = sortedHomes.containsKey(w) ? sortedHomes.get(w) : new ArrayList<Home>();
            homes.add(h);
            sortedHomes.put(h.getLocation().getWorld(), homes);
        }
        final int homeLimit = rp.getHomeLimit();
        cs.sendMessage(MessageColor.POSITIVE + "Homes (" + MessageColor.NEUTRAL + rp.getHomes().size() + MessageColor.POSITIVE + "/" + MessageColor.NEUTRAL + ((homeLimit < 0) ? "Unlimited" : homeLimit) + MessageColor.POSITIVE + "):");
        for (final Map.Entry<World, List<Home>> entry : sortedHomes.entrySet()) {
            final World w = entry.getKey();
            final TextComponent tc = new TextComponent("  Homes for ");
            tc.setColor(MessageColor.POSITIVE.bc());
            final TextComponent tcw = new TextComponent(w == null ? "an invalid world" : RUtils.getMVWorldName((w)));
            tcw.setColor(MessageColor.NEUTRAL.bc());
            Text wtt = new Text(w == null ? MessageColor.NEGATIVE + "Cannot teleport here" : MessageColor.POSITIVE + "Click to teleport" + "\nto " + MessageColor.NEUTRAL + RUtils.getMVWorldName((w)));
            tcw.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, wtt));
            if (w != null) {
                tcw.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/world " + w.getName()));
                TextComponent tcc = new TextComponent(":");
                tcc.setColor(MessageColor.POSITIVE.bc());
                tcw.addExtra(tcc);
	        }
            tc.addExtra(tcw);

            cs.spigot().sendMessage(tc);
            final TextComponent tch = new TextComponent("    ");
            final Iterator<Home> homes = entry.getValue().iterator();
            while (homes.hasNext()) {
                final Home home = homes.next();
                TextComponent tchh = new TextComponent(home.getName());
                tchh.setColor(w == null ? MessageColor.NEGATIVE.bc() : MessageColor.NEUTRAL.bc());
                Text htt = new Text(w == null ? MessageColor.NEGATIVE + "Cannot teleport here" : MessageColor.POSITIVE + "Click to teleport" + "\nto " + MessageColor.NEUTRAL + home.getName());
                tchh.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, htt));
                tchh.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + home.getFullName()));
                tch.addExtra(tchh);
                if (homes.hasNext()) tch.addExtra(MessageColor.RESET + ", "); // it's not a color OR a style
            }
            cs.spigot().sendMessage(tch);
        }
        return true;
    }
}
