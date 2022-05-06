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
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

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
            final FancyMessage fm = new FancyMessage("  Homes for ").color(MessageColor.POSITIVE.cc()).then(w == null ? "an invalid world" : w.getName()).color(MessageColor.NEUTRAL.cc()).tooltip(w == null ? MessageColor.NEGATIVE + "Cannot teleport here" : MessageColor.POSITIVE + "Click to teleport" + "\nto " + MessageColor.NEUTRAL + w.getName()).command("/world " + w.getName()).then(":").color(MessageColor.POSITIVE.cc());
            fm.send(cs);
            final FancyMessage fmh = new FancyMessage("    ");
            final Iterator<Home> homes = entry.getValue().iterator();
            while (homes.hasNext()) {
                final Home home = homes.next();
                fmh.then(home.getName()).color(w == null ? MessageColor.NEGATIVE.cc() : MessageColor.NEUTRAL.cc()).tooltip(w == null ? MessageColor.NEGATIVE + "Cannot teleport here" : MessageColor.POSITIVE + "Click to teleport" + "\nto " + MessageColor.NEUTRAL + home.getName()).command("/home " + home.getFullName());
                if (homes.hasNext()) fmh.then(MessageColor.RESET + ", "); // it's not a color OR a style
            }
            fmh.send(cs);
        }
        return true;
    }
}
