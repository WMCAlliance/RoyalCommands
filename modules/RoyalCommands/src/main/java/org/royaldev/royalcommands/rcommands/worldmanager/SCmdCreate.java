/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.worldmanager;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdWorldManager;
import org.royaldev.royalcommands.rcommands.SubCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SCmdCreate extends SubCommand<CmdWorldManager> {

    public SCmdCreate(final RoyalCommands instance, final CmdWorldManager parent) {
        super(instance, parent, "create", true, "Creates a new world.", "<command> [name] (type) (environment) (seed) (generator)", new String[0], new Short[]{CompletionType.NONE.getShort(), CompletionType.LIST.getShort(), CompletionType.ENUM.getShort(), CompletionType.NONE.getShort(), CompletionType.NONE.getShort()});
    }

    @Override
    public List<String> customList(CommandSender cs, Command cmd, String label, String[] args, String arg) {
        final List<String> completions = new ArrayList<>();
        if (args.length < 2) return completions;
        for (final WorldType wt : WorldType.values()) {
            completions.add(wt.getName().toLowerCase());
        }
        return completions;
    }

    @Override
    protected Environment[] customEnum(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return Environment.values();
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, final CommandArguments ca) {
        if (!Config.useWorldManager) {
            cs.sendMessage(MessageColor.NEGATIVE + "WorldManager is disabled!");
            return true;
        }
        if (args.length < 1) {
            cs.sendMessage("Args: " + args.length + ": " + Arrays.toString(args));
            cs.sendMessage(MessageColor.NEGATIVE + "Not enough arguments! Try " + MessageColor.NEUTRAL + "/" + label + MessageColor.NEGATIVE + " for help.");
            return true;
        }
        String name = args[0];
        final WorldType type = WorldType.getByName(args.length > 1 ? args[1] : "default");
        final Environment we;
        try {
            we = Environment.valueOf((args.length > 2 ? args[2] : "normal").toUpperCase());
        } catch (IllegalArgumentException ex) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid environment!");
            String types = "";
            for (Environment t : Environment.values())
                types = (types.equals("")) ? types.concat(MessageColor.NEUTRAL + t.name() + MessageColor.RESET) : types.concat(", " + MessageColor.NEUTRAL + t.name().toLowerCase() + MessageColor.RESET);
            cs.sendMessage(types);
            return true;
        }
        for (World w : this.plugin.getServer().getWorlds()) {
            if (w.getName().equals(name)) {
                cs.sendMessage(MessageColor.NEGATIVE + "A world with that name already exists!");
                return true;
            }
        }
        if (type == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid world type!");
            String types = "";
            for (WorldType t : WorldType.values())
                types = (types.equals("")) ? types.concat(MessageColor.NEUTRAL + t.getName() + MessageColor.RESET) : types.concat(", " + MessageColor.NEUTRAL + t.getName().toLowerCase() + MessageColor.RESET);
            cs.sendMessage(types);
            return true;
        }
        cs.sendMessage(MessageColor.POSITIVE + "Creating world " + MessageColor.NEUTRAL + name + MessageColor.POSITIVE + "...");
        WorldCreator wc = new WorldCreator(name);
        wc = wc.type(type);
        wc = wc.environment(we);
        if (args.length > 3) {
            final String seedString = args[3];
            long seed;
            try {
                seed = Long.valueOf(seedString);
            } catch (Exception e) {
                seed = seedString.hashCode();
            }
            wc = wc.seed(seed);
        } else wc = wc.seed(this.getParent().getRandom().nextLong());
        if (args.length > 4) {
            final String generator = args[4];
            wc = wc.generator(generator);
            RoyalCommands.wm.getConfig().set("worlds." + name + ".generator", generator);
        }
        World w = wc.createWorld();
        w.save();
        cs.sendMessage(MessageColor.POSITIVE + "World " + MessageColor.NEUTRAL + w.getName() + MessageColor.POSITIVE + " created successfully.");
        return true;
    }
}
