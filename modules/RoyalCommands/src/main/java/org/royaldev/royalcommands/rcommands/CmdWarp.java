/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.Configuration;
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

@ReflectCommand
public class CmdWarp extends TabCommand {

    public CmdWarp(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.WARP.getShort(), CompletionType.ONLINE_PLAYER.getShort()});
    }

    public static Location pWarp(Player p, String name) {
        boolean warpSet;
        Double warpX;
        Double warpY;
        Double warpZ;
        Float warpYaw;
        Float warpPitch;
        World warpW;

        Configuration cm = Configuration.getConfiguration("warps.yml");
        if (!cm.exists()) return null;
        warpSet = cm.getBoolean("warps." + name + ".set");
        if (!warpSet) return null;
        warpX = cm.getDouble("warps." + name + ".x");
        warpY = cm.getDouble("warps." + name + ".y");
        warpZ = cm.getDouble("warps." + name + ".z");
        warpYaw = Float.parseFloat(cm.getString("warps." + name + ".yaw"));
        warpPitch = Float.parseFloat(cm.getString("warps." + name + ".pitch"));
        warpW = Bukkit.getServer().getWorld(cm.getString("warps." + name + ".w"));
        if (warpW == null) {
            p.sendMessage(MessageColor.NEGATIVE + "World doesn't exist!");
            return null;
        }
        try {
            return new Location(warpW, warpX, warpY, warpZ, warpYaw, warpPitch);
        } catch (Exception e) {
            p.sendMessage(MessageColor.NEGATIVE + "There are no warps!");
            return null;
        }
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {

        if (!(cs instanceof Player) && args.length < 2) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }

        if (args.length < 1) {
            Configuration cm = Configuration.getConfiguration("warps.yml");
            if (!cm.exists() || cm.get("warps") == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "There are no warps!");
                return true;
            }
            final Map<String, Object> opts = cm.getConfigurationSection("warps").getValues(false);
            if (opts.keySet().isEmpty()) {
                cs.sendMessage(MessageColor.NEGATIVE + "There are no warps!");
                return true;
            }
            Iterator<String> warps = opts.keySet().iterator();
            cs.sendMessage(MessageColor.POSITIVE + "Warps:");
            final FancyMessage fm = new FancyMessage("");
            while (warps.hasNext()) {
                final String warp = warps.next();
                fm.then(warp).color(MessageColor.NEUTRAL.cc()).tooltip(MessageColor.POSITIVE + "Click to teleport" + "\nto " + MessageColor.NEUTRAL + warp).command("/warp " + warp);
                if (warps.hasNext()) fm.then(MessageColor.RESET + ", "); // it's not a color OR a style
            }
            fm.send(cs);
            return true;
        }
        if (args.length == 1 && cs instanceof Player) {
            Player p = (Player) cs;
            Location warpLoc = pWarp(p, args[0].toLowerCase());
            if (warpLoc == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "No such warp!");
                return true;
            }
            if (Config.warpPermissions && !this.ah.isAuthorized(cs, "rcmds.warp." + args[0].toLowerCase())) {
                cs.sendMessage(MessageColor.NEGATIVE + "You do not have permission for that warp!");
                return true;
            }
            cs.sendMessage(MessageColor.POSITIVE + "Teleported to warp " + MessageColor.NEUTRAL + args[0].toLowerCase() + MessageColor.POSITIVE + ".");
            final RPlayer rp = MemoryRPlayer.getRPlayer(p);
            String error = rp.getTeleporter().teleport(warpLoc);
            if (!error.isEmpty()) {
                p.sendMessage(MessageColor.NEGATIVE + error);
                return true;
            }
            return true;
        }
        if (args.length > 1) {
            if (!this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
                RUtils.dispNoPerms(cs);
                return true;
            }
            Player t = this.plugin.getServer().getPlayer(args[1]);
            if (t == null || this.plugin.isVanished(t, cs)) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You cannot warp that player!");
                return true;
            }
            cs.sendMessage(MessageColor.POSITIVE + "Warping " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + " \"" + MessageColor.NEUTRAL + args[0].toLowerCase() + MessageColor.POSITIVE + ".\"");
            Location warpLoc = pWarp(t, args[0].toLowerCase());
            if (warpLoc == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "No such warp!");
                return true;
            }
            if (Config.warpPermissions && !this.ah.isAuthorized(t, "rcmds.warp." + args[0].toLowerCase())) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not have permission for that warp!");
                return true;
            }
            final RPlayer rp = MemoryRPlayer.getRPlayer(t);
            String error = rp.getTeleporter().teleport(warpLoc);
            if (!error.isEmpty()) {
                cs.sendMessage(MessageColor.NEGATIVE + error);
                return true;
            }
            return true;
        }
        return true;
    }
}
