/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

@ReflectCommand
public class CmdBack extends TabCommand {

    private static final Map<UUID, List<Location>> backdb = new HashMap<>();
    private final DecimalFormat df = new DecimalFormat("0.00");

    public CmdBack(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{TabCommand.CompletionType.LIST.getShort()});
    }

    /**
     * Adds a location to the /back stack. If there are too many locations, the last (oldest) one will be removed.
     *
     * @param p     Player to add a location for
     * @param toAdd Location to add
     */
    public static void addBackLocation(Player p, Location toAdd) {
        if (Config.disabledBackWorlds.contains(toAdd.getWorld().getName())) return;
        int maxStack = Config.maxBackStack;
        synchronized (backdb) {
            List<Location> backs = backdb.get(p.getUniqueId());
            if (backs == null) backs = new ArrayList<>();
            // remove last location if needed
            if (backs.size() > 0 && backs.size() >= maxStack) backs.remove(backs.size() - 1);
            backs.add(0, toAdd);
            backdb.put(p.getUniqueId(), backs);
        }
    }
	
	@Override
	protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
		if (!(cs instanceof Player)) return new ArrayList<>();
		Player player = (Player) cs;
        synchronized (backdb) {
            List<Location> backs = backdb.get(player.getUniqueId());
            if (backs == null) return new ArrayList<>();
			String [] array = new String[backs.size()];
			for (int a = 0; a < array.length; a++) {
				array[a] = Integer.toString(a + 1);
			}
			return new ArrayList<>(Arrays.asList(array));
		}
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        final Player p = (Player) cs;
        final RPlayer rp = MemoryRPlayer.getRPlayer(p);
        if (!backdb.containsKey(p.getUniqueId())) {
            cs.sendMessage(MessageColor.NEGATIVE + "You have no place to go back to!");
            return true;
        }
        if ("backs".equalsIgnoreCase(label)) {
            final List<Location> backs = backdb.get(p.getUniqueId());
            cs.sendMessage(MessageColor.NEUTRAL + "/back locations:");
            for (int i = 0; i < backs.size(); i++) {
                Location l = backs.get(i);
                if (l == null) continue;
                final Block b = l.getBlock().getRelative(BlockFace.DOWN);
                // @formatter:off
                RUtils.addCommandTo(new FancyMessage("  ")
                    .then(i + 1 + ": ")
                        .color(MessageColor.NEUTRAL.cc())
                    .then("on ")
                        .color(MessageColor.POSITIVE.cc())
                    .then(RUtils.getItemName(b.getType()))
                        .color(MessageColor.NEUTRAL.cc())
                    .then(" in ")
                        .color(MessageColor.POSITIVE.cc())
                    .then(RUtils.getFriendlyEnumName(b.getBiome()))
                        .color(MessageColor.NEUTRAL.cc())
                    .then(" (")
                        .color(MessageColor.POSITIVE.cc())
                    .then(RUtils.getMVWorldName(l.getWorld()))
                        .color(MessageColor.NEUTRAL.cc())
                    .then(", ")
                        .color(MessageColor.POSITIVE.cc())
                    .then(this.df.format(l.getX()))
                        .color(MessageColor.NEUTRAL.cc())
                    .then(", ")
                        .color(MessageColor.POSITIVE.cc())
                    .then(this.df.format(l.getY()))
                        .color(MessageColor.NEUTRAL.cc())
                    .then(", ")
                        .color(MessageColor.POSITIVE.cc())
                    .then(this.df.format(l.getZ()))
                        .color(MessageColor.NEUTRAL.cc())
                    .then(")")
                        .color(MessageColor.POSITIVE.cc()), "/back " + (i + 1))
                    .send(cs);
                // @formatter:on
            }
            return true;
        }
        int index = 0;
        try {
            if (args.length > 0) {
                index = Integer.parseInt(args[0]);
                index--;
            }
        } catch (NumberFormatException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "The back number was not a valid number!");
            return true;
        }
        List<Location> backs = backdb.get(p.getUniqueId());
        if (index < 0 || index >= backs.size()) {
            cs.sendMessage(MessageColor.NEGATIVE + "No such back number!");
            return true;
        }
        final String error = rp.getTeleporter().teleport(backs.get(index));
        if (!error.isEmpty()) {
            p.sendMessage(MessageColor.NEGATIVE + error);
            return true;
        }
        p.sendMessage(MessageColor.POSITIVE + "Returning to your previous location.");
        return true;
    }
}
