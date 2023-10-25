/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.Configuration;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

@ReflectCommand
public class CmdJail extends TabCommand {

    public final Map<UUID, Location> jaildb = new HashMap<>();

    public CmdJail(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort(), CompletionType.LIST.getShort()});
    }
	
    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        final Configuration cm = Configuration.getConfiguration("jails.yml");
		if (!cm.exists() || cm.get("jails") == null) return new ArrayList<>();
        final Map<String, Object> opts = cm.getConfigurationSection("jails").getValues(false);
		ArrayList<String> jails = new ArrayList<>();
		jails.addAll(opts.keySet());
		return jails;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        Configuration cm = Configuration.getConfiguration("jails.yml");

        if (args.length < 1) {
            if (!cm.exists() || cm.get("jails") == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "There are no jails!");
                return true;
            }
            final Map<String, Object> opts = cm.getConfigurationSection("jails").getValues(false);
            if (opts.keySet().isEmpty()) {
                cs.sendMessage(MessageColor.NEGATIVE + "There are no jails!");
                return true;
            }
            String jails = opts.keySet().toString();
            jails = jails.substring(1, jails.length() - 1);
            cs.sendMessage(MessageColor.POSITIVE + "Jails:");
            cs.sendMessage(jails);
            return true;
        }

        Player t = this.plugin.getServer().getPlayer(args[0]);
        if (t == null || this.plugin.isVanished(t, cs)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        if (this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You cannot jail that player.");
            return true;
        }
        PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(t);
        if (args.length < 2) {
            if (pcm.getBoolean("jailed")) {
                pcm.set("jailed", false);
                cs.sendMessage(MessageColor.POSITIVE + "You have released " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + ".");
                t.sendMessage(MessageColor.POSITIVE + "You have been released.");
                if (this.jaildb.get(t.getUniqueId()) == null || this.jaildb.get(t.getUniqueId()).getWorld() == null) {
                    t.sendMessage(MessageColor.NEGATIVE + "Your previous location no longer exists. Sending you to spawn.");
                    final RPlayer rp = MemoryRPlayer.getRPlayer(t);
                    String error = rp.getTeleporter().teleport(CmdSpawn.getWorldSpawn(t.getWorld()), true);
                    if (!error.isEmpty()) {
                        cs.sendMessage(MessageColor.NEGATIVE + error);
                        return true;
                    }
                    return true;
                }
                final RPlayer rp = MemoryRPlayer.getRPlayer(t);
                String error = rp.getTeleporter().teleport(this.jaildb.get(t.getUniqueId()), true);
                if (!error.isEmpty()) {
                    cs.sendMessage(MessageColor.NEGATIVE + error);
                    return true;
                }
                return true;
            }
            cs.sendMessage(cmd.getDescription());
            return false;
        }

        boolean jailSet;
        Double jailX;
        Double jailY;
        Double jailZ;
        Float jailYaw;
        Float jailPitch;
        World jailW;

        if (!cm.exists()) {
            cs.sendMessage(MessageColor.NEGATIVE + "No jails set!");
            return true;
        }
        if (args.length < 1) {
            if (cm.get("jails") == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "There are no jails!");
                return true;
            }
            final Map<String, Object> opts = cm.getConfigurationSection("jails").getValues(false);
            if (opts.keySet().isEmpty()) {
                cs.sendMessage(MessageColor.NEGATIVE + "There are no jails!");
                return true;
            }
            String jails = opts.keySet().toString();
            jails = jails.substring(1, jails.length() - 1);
            cs.sendMessage(MessageColor.POSITIVE + "Jails:");
            cs.sendMessage(jails);
            return true;
        }
        jailSet = cm.getBoolean("jails." + args[1] + ".set");
        if (jailSet) {
            jailX = cm.getDouble("jails." + args[1] + ".x");
            jailY = cm.getDouble("jails." + args[1] + ".y");
            jailZ = cm.getDouble("jails." + args[1] + ".z");
            jailYaw = Float.parseFloat(cm.getString("jails." + args[1] + ".yaw"));
            jailPitch = Float.parseFloat(cm.getString("jails." + args[1] + ".pitch"));
            jailW = this.plugin.getServer().getWorld(cm.getString("jails." + args[1] + ".w"));
        } else {
            cs.sendMessage(MessageColor.NEGATIVE + "That jail does not exist.");
            return true;
        }
        Location jailLoc = new Location(jailW, jailX, jailY, jailZ, jailYaw, jailPitch);
        if (pcm.getBoolean("jailed")) {
            pcm.set("jailed", false);
            cs.sendMessage(MessageColor.POSITIVE + "You have released " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + ".");
            t.sendMessage(MessageColor.POSITIVE + "You have been released.");
            if (this.jaildb.get(t.getUniqueId()) == null || this.jaildb.get(t.getUniqueId()).getWorld() == null) {
                t.sendMessage(MessageColor.NEGATIVE + "Your previous location no longer exists. Sending you to spawn.");
                final RPlayer rp = MemoryRPlayer.getRPlayer(t);
                String error = rp.getTeleporter().teleport(CmdSpawn.getWorldSpawn(t.getWorld()), true);
                if (!error.isEmpty()) {
                    cs.sendMessage(MessageColor.NEGATIVE + error);
                    return true;
                }
                return true;
            }
            final RPlayer rp = MemoryRPlayer.getRPlayer(t);
            String error = rp.getTeleporter().teleport(this.jaildb.get(t.getUniqueId()), true);
            if (!error.isEmpty()) {
                cs.sendMessage(MessageColor.NEGATIVE + error);
                return true;
            }
            return true;
        } else {
            if (jailW == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "World doesn't exist!");
            }
            cs.sendMessage(MessageColor.POSITIVE + "You have jailed " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + ".");
            t.sendMessage(MessageColor.NEGATIVE + "You have been jailed.");
            this.jaildb.put(t.getUniqueId(), t.getLocation());
            final RPlayer rp = MemoryRPlayer.getRPlayer(t);
            String error = rp.getTeleporter().teleport(jailLoc, true);
            if (!error.isEmpty()) {
                cs.sendMessage(MessageColor.NEGATIVE + error);
                return true;
            }
            pcm.set("jailed", true);
            return true;
        }
    }
}
