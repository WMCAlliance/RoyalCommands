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
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.List;


@ReflectCommand
public class CmdMessageOfTheDay extends TabCommand {

    private static RoyalCommands pluginInstance;

    public CmdMessageOfTheDay(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
        CmdMessageOfTheDay.pluginInstance = instance;
    }

    public static void showMotd(CommandSender cs, List<String> config) {
        String ps = CmdList.getSimpleList(cs);
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
        for (String s : config) {
            if (s == null) continue;
            s = RUtils.colorize(s);
            s = s.replace("{name}", cs.getName());
            s = (cs instanceof Player) ? s.replace("{dispname}", ((Player) cs).getDisplayName()) : s.replace("{dispname}", cs.getName());
            if (onlinenum != null) s = s.replace("{players}", onlinenum);
            s = s.replace("{playerlist}", ps);
            s = (cs instanceof Player) ? s.replace("{time24h}", RUtils.getWorldTime24Hour(((Player) cs).getWorld())) : s.replace("{time24h}", "No Time");
            s = (cs instanceof Player) ? s.replace("{time12h}", RUtils.getWorldTime12Hour(((Player) cs).getWorld())) : s.replace("{time12h}", "No Time");
            s = (cs instanceof Player) ? s.replace("{world}", RUtils.getMVWorldName(((Player) cs).getWorld())) : s.replace("{world}", "No World");
            s = s.replace("{uptime}", RUtils.formatDateDiff(pluginInstance.getStartTime()));
            if (maxonl != null) s = s.replace("{maxplayers}", maxonl);
            if (cs instanceof Player && CmdMessageOfTheDay.pluginInstance.pa != null) s = PlaceholderAPI.setPlaceholders((Player)cs, s);
            s = s.replace("{servername}", CmdMessageOfTheDay.pluginInstance.getServer().getName());
            cs.sendMessage(s);
        }
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        List<String> config;
        if (Config.useAdminMotd && ah.isAuthorized(cs, "rcmds.messageoftheday.admin")) {
            config = Config.motdAdmin;
        } else {
            config = Config.motdGeneral;
        }
        CmdMessageOfTheDay.showMotd(cs, config);
        return true;
    }
}
