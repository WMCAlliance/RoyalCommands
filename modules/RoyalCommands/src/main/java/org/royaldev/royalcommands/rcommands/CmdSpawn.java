/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.Configuration;

@ReflectCommand
public class CmdSpawn extends TabCommand {

    public CmdSpawn(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    /**
     * Gets the group-specific spawn for a player and a world.
     *
     * @param p     Player to get spawn for
     * @param world World to get spawn for
     * @return null if no group-specific spawn or Location if existent
     */
    private static Location getGroupSpawn(Player p, World world) {
        Configuration cm = Configuration.getConfiguration("spawns.yml");
        String group;
        try {
            if (!RoyalCommands.getInstance().vh.usingVault()) throw new UnsupportedOperationException();
            group = RoyalCommands.getInstance().vh.getPermission().getPrimaryGroup(p);
        } catch (UnsupportedOperationException e) {
            group = null;
        } catch (NullPointerException e) {
            group = null;
        }
        if (group == null || group.isEmpty()) return null;
        group = "." + group.toLowerCase();
        return cm.getLocation("spawns." + world.getName() + group, world.getName());
    }

    /**
     * Get the custom spawn location of a world. If none is set, it will return the default.
     *
     * @param world World to get spawn for
     * @return Custom spawn or default spawn if not set
     */
    public static Location getWorldSpawn(World world) {
        Configuration cm = Configuration.getConfiguration("spawns.yml");
        String w = world.getName();
        Location l = cm.getLocation("spawns." + w, world.getName());
        if (l == null) l = world.getSpawnLocation();
        return l;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        World w;
        if (args.length > 0) {
            if (!this.ah.isAuthorized(cs, "rcmds.spawn.other")) {
                cs.sendMessage(MessageColor.NEGATIVE + "You don't have permission to spawn in other worlds.");
                return true;
            }
            w = this.plugin.getServer().getWorld(args[0]);
            if (w == null || Config.hiddenWorlds.contains(w.getName())) {
                cs.sendMessage(MessageColor.NEGATIVE + "No such world!");
                return true;
            }
        } else w = p.getWorld();
        Location l = getGroupSpawn(p, w);
        if (l == null) l = getWorldSpawn(w);
        p.sendMessage(MessageColor.POSITIVE + "Going to spawn in " + MessageColor.NEUTRAL + RUtils.getMVWorldName(w) + MessageColor.POSITIVE + ".");
        String error = RUtils.teleport(p, l);
        if (!error.isEmpty()) p.sendMessage(MessageColor.NEGATIVE + error);
        return true;
    }
}
