/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdMessageOfTheDay extends TabCommand {

    private static RoyalCommands pluginInstance;

    public CmdMessageOfTheDay(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
        CmdMessageOfTheDay.pluginInstance = instance;
    }

    public static void showMotd(CommandSender cs) {
        String ps = (Config.simpleList) ? CmdList.getSimpleList(cs) : RUtils.join(CmdList.getGroupList(cs), "\n");
        int onnum = CmdMessageOfTheDay.pluginInstance.getServer().getOnlinePlayers().size();
        int hid = CmdMessageOfTheDay.pluginInstance.getNumberVanished();
        String onlinenum;
        try {
            onlinenum = Integer.toString(onnum - hid);
        } catch (Exception e) {
            onlinenum = null;
        }
        Integer maxon = CmdMessageOfTheDay.pluginInstance.getServer().getMaxPlayers();
        String maxonl;
        try {
            maxonl = Integer.toString(maxon);
        } catch (Exception e) {
            maxonl = null;
        }
        for (String s : Config.motd) {
            if (s == null) continue;
            s = RUtils.colorize(s);
            s = s.replace("{name}", cs.getName());
            s = (cs instanceof Player) ? s.replace("{dispname}", ((Player) cs).getDisplayName()) : s.replace("{dispname}", cs.getName());
            if (onlinenum != null) s = s.replace("{players}", onlinenum);
            s = s.replace("{playerlist}", ps);
            s = (cs instanceof Player) ? s.replace("{world}", RUtils.getMVWorldName(((Player) cs).getWorld())) : s.replace("{world}", "No World");
            if (maxonl != null) s = s.replace("{maxplayers}", maxonl);
            s = (CmdMessageOfTheDay.pluginInstance.getServer().getName() != null || !CmdMessageOfTheDay.pluginInstance.getServer().getName().isEmpty()) ? s.replace("{servername}", CmdMessageOfTheDay.pluginInstance.getServer().getName()) : s.replace("{servername}", "this server");
            cs.sendMessage(s);
        }
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        CmdMessageOfTheDay.showMotd(cs);
        return true;
    }
}
