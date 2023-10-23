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
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;

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
            final FancyMessage fm = new FancyMessage("");
            while (worlds.hasNext()) {
                final World world = worlds.next();
                if (Config.hiddenWorlds.contains(world.getName())) continue;
                fm.then(RUtils.getMVWorldName(world)).color(MessageColor.NEUTRAL.cc()).tooltip(MessageColor.POSITIVE + "Click to teleport" + "\nto " + MessageColor.NEUTRAL + RUtils.getMVWorldName(world)).command("/tpw " + world.getName());
                if (worlds.hasNext()) fm.then(MessageColor.RESET + ", "); // it's not a color OR a style
            }
            fm.send(cs);
            return true;
        }
        if (Config.hiddenWorlds.contains(w.getName())) return true;
        Player p = (Player) cs;
        p.sendMessage(MessageColor.POSITIVE + "Teleporting you to world " + MessageColor.NEUTRAL + RUtils.getMVWorldName(w) + MessageColor.POSITIVE + ".");
        String error = RUtils.teleport(p, CmdSpawn.getWorldSpawn(w));
        if (!error.isEmpty()) {
            p.sendMessage(MessageColor.NEGATIVE + error);
            return true;
        }
        return true;
    }
}
