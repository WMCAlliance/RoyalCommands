/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdBiome extends TabCommand {

    public CmdBiome(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.BIOME.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        if (args.length < 1) {
            cs.sendMessage(MessageColor.POSITIVE + "Biomes:");
            sendBiomeList(cs);
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        final Player p = (Player) cs;
        final Biome b;
        try {
            b = Registry.BIOME.getOrThrow(NamespacedKey.fromString(args[0]));
        } catch (IllegalArgumentException e) {
            p.sendMessage(MessageColor.NEGATIVE + "No such biome!");
            sendBiomeList(p);
            return true;
        }
        int rradius = 1;
        if (args.length > 1) {
            try {
                rradius = Integer.valueOf(args[1]);
            } catch (NumberFormatException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Invalid radius!");
                return true;
            }
        }
        final int radius = rradius;
        final Chunk c = p.getLocation().getChunk();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        final Chunk ac = p.getLocation().getWorld().getChunkAt(c.getX() + x, c.getZ() + z);
                        if (!ac.isLoaded()) ac.load(true);
                        for (int cx = 0; cx < 16; cx++) {
                            for (int cz = 0; cz < 16; cz++) {
                                for (int cy = 0; cy < c.getWorld().getMaxHeight(); cy++) {
                                    ac.getBlock(cx, cy, cz).setBiome(b);
                                }
                            }
                        }
                        ac.getWorld().refreshChunk(ac.getX(), ac.getZ());
                    }
                }
                p.sendMessage(MessageColor.POSITIVE + "Set biome" + ((radius > 1) ? "s" : "") + " to " + MessageColor.NEUTRAL + RUtils.getBiomeName(b) + MessageColor.POSITIVE + ".");
            }
        };
        p.sendMessage(MessageColor.POSITIVE + "Changing biomes. This may take a moment..");
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, r, 0);
        return true;
    }

    public void sendBiomeList(CommandSender cs) {
        String biomes = "";
        for (Biome b : Registry.BIOME) {
            String name = MessageColor.NEUTRAL + b.getKeyOrNull().getKey() + MessageColor.RESET;
            biomes = (biomes.equals("")) ? biomes.concat(name) : biomes.concat(", " + name);
        }
        cs.sendMessage(biomes);
    }
}
