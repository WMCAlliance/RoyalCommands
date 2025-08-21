/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdWeather extends TabCommand {

    public CmdWeather(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ENUM.getShort()});
    }

    public static boolean changeWeather(Player p, WeatherType wt) {
        return CmdWeather.changeWeather(p, wt, null);
    }

    public static boolean changeWeather(Player p, WeatherType wt, Integer length) {
        if (wt == null) {
            p.sendMessage(MessageColor.NEGATIVE + "Invalid condition!");
            return false;
        }
        if (length != null && length < 1) {
            p.sendMessage(MessageColor.NEGATIVE + "The time specified was invalid!");
            return false;
        }
        World world = p.getWorld();
        String wName = RUtils.getMVWorldName(world);
        world.setStorm(wt.isStorm());
        world.setThundering(wt.isThundering());
        if (length != null) world.setWeatherDuration(length);
        p.sendMessage(MessageColor.POSITIVE + "Set weather to " + MessageColor.NEUTRAL + RUtils.getFriendlyEnumName(wt) + MessageColor.POSITIVE + " in " + MessageColor.NEUTRAL + wName + MessageColor.POSITIVE + ".");
        return true;
    }

    public static boolean validWeather(String conds) {
        return conds.toLowerCase().startsWith("sun") || conds.toLowerCase().startsWith("rain") || conds.toLowerCase().startsWith("storm");
    }

    @Override
    public WeatherType[] customEnum(CommandSender cs, Command cmd, String label, String[] args, String arg) {
        return WeatherType.values();
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (eargs.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        String conds = eargs[0];
        Integer length = eargs.length > 1 ? RUtils.timeFormatToSeconds(eargs[1]) : null;
        if (length != null && length <= 0) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid time specified.");
            return true;
        }
        CmdWeather.changeWeather(p, WeatherType.getWeatherType(conds), length);
        return true;
    }

    public enum WeatherType {
        CLEAR(false, false),
        RAIN(true, false),
        STORM(true, true);

        private final boolean storm, thundering;

        private WeatherType(boolean storm, boolean thundering) {
            this.storm = storm;
            this.thundering = thundering;
        }

        public static WeatherType getWeatherType(String s) {
            s = s.toLowerCase();
            if (s.startsWith("clear") || s.startsWith("sun")) return WeatherType.CLEAR;
            else if (s.startsWith("rain")) return WeatherType.RAIN;
            else if (s.startsWith("storm")) return WeatherType.STORM;
            return null;
        }

        public boolean isStorm() {
            return this.storm;
        }

        public boolean isThundering() {
            return this.thundering;
        }
    }
}
