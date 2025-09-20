/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.worldmanager;

import java.text.DecimalFormat;

import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.Configuration;
import org.royaldev.royalcommands.rcommands.CmdWorldManager;
import org.royaldev.royalcommands.rcommands.SubCommand;
import org.royaldev.royalcommands.rcommands.TabCommand;

public class SCmdInfo extends SubCommand<CmdWorldManager> {

    public SCmdInfo(final RoyalCommands instance, final CmdWorldManager parent) {
        super(instance, parent, "info", true, "Displays available world types and environments; if you are a player, displays information about your world.", "<command> (name)", new String[0], new Short[]{TabCommand.CompletionType.WORLD.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, final CommandArguments ca) {
        if (!Config.useWorldManager) {
            cs.sendMessage(MessageColor.NEGATIVE + "WorldManager is disabled!");
            return true;
        }
        cs.sendMessage(MessageColor.POSITIVE + "RoyalCommands WorldManager Info");
        cs.sendMessage(MessageColor.POSITIVE + "===============================");
        cs.sendMessage(MessageColor.POSITIVE + "Available world types:");
        final StringBuilder types = new StringBuilder("  ");
        for (WorldType t : WorldType.values()) {
            types.append(MessageColor.NEUTRAL).append(t.getName()).append(MessageColor.RESET).append(", ");
        }
        cs.sendMessage(types.substring(0, types.length() - 4));
        cs.sendMessage(MessageColor.POSITIVE + "Available world environments:");
        final StringBuilder envs = new StringBuilder("  ");
        for (Environment e : Environment.values()) {
            envs.append(MessageColor.NEUTRAL).append(e.name()).append(MessageColor.RESET).append(", ");
        }
        cs.sendMessage(envs.substring(0, envs.length() - 4));
        World w = null;
        if (args.length > 0) {
            w = this.plugin.getServer().getWorld(args[0]);
        } else if (cs instanceof Player) {
            final Player p = (Player) cs;
            w = p.getWorld();
        }
        if (w == null) return true;
        DecimalFormat df = new DecimalFormat("#.##");
        Location l = w.getSpawnLocation();
        Configuration cm = RoyalCommands.wm.getConfig();
        Boolean timeFrozen = cm.getBoolean("worlds." + w.getName() + ".freezetime");
        cs.sendMessage(MessageColor.POSITIVE + "Information on this world:");
        cs.sendMessage(MessageColor.POSITIVE + "  Name: " + MessageColor.NEUTRAL + w.getName());
        cs.sendMessage(MessageColor.POSITIVE + "  Display Name: " + MessageColor.NEUTRAL + RUtils.getMVWorldName(w));
        cs.sendMessage(MessageColor.POSITIVE + "  UID: " + MessageColor.NEUTRAL + w.getUID());
        cs.sendMessage(MessageColor.POSITIVE + "  Game Mode: " + MessageColor.NEUTRAL + cm.getString("worlds." + w.getName() + ".gamemode", "SURVIVAL").toLowerCase());
        // TODO Seed, but hidden behind a tooltip
        // cs.sendMessage(MessageColor.POSITIVE + "  Seed: " + MessageColor.NEUTRAL + w.getSeed());
        cs.sendMessage(MessageColor.POSITIVE + "  Spawn Location: " + MessageColor.NEUTRAL + df.format(l.getX()) + MessageColor.POSITIVE + ", " + MessageColor.NEUTRAL + df.format(l.getY()) + MessageColor.POSITIVE + ", " + MessageColor.NEUTRAL + df.format(l.getZ()) + MessageColor.POSITIVE);
        cs.sendMessage(MessageColor.POSITIVE + "  Environment: " + MessageColor.NEUTRAL + w.getEnvironment().name().toLowerCase());
        cs.sendMessage(MessageColor.POSITIVE + "  Generator: " + MessageColor.NEUTRAL + (w.getGenerator() != null ? w.getGenerator().toString() : "default"));
        cs.sendMessage(MessageColor.POSITIVE + "  Generate Structures: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(w.canGenerateStructures()));
        cs.sendMessage(MessageColor.POSITIVE + "  Difficulty: " + MessageColor.NEUTRAL + (w.getDifficulty().toString().toLowerCase()));
        cs.sendMessage(MessageColor.POSITIVE + "  Animals: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(w.getAllowAnimals()));
        cs.sendMessage(MessageColor.POSITIVE + "  Monsters: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(w.getAllowMonsters()));
        cs.sendMessage(MessageColor.POSITIVE + "  PVP: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(w.getPVP()));
        cs.sendMessage(MessageColor.POSITIVE + "  Time Frozen: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(timeFrozen));
        cs.sendMessage(MessageColor.POSITIVE + "  Spawn Limits:");
        for (SpawnCategory cat : SpawnCategory.values()) {
            String catName = RUtils.getFriendlyEnumName(cat);
            try {
                cs.sendMessage(MessageColor.POSITIVE + "   - " + catName + ": " + MessageColor.NEUTRAL + w.getSpawnLimit(cat));
            } catch (IllegalArgumentException ex) {
                cs.sendMessage(MessageColor.POSITIVE + "   - " + catName + ": " + MessageColor.NEUTRAL + "not supported");
            }
        }
        return true;
    }
}
