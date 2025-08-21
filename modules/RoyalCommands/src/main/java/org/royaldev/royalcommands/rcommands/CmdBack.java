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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.back.Back;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

@ReflectCommand
public class CmdBack extends TabCommand {

    private static final Map<UUID, List<Back>> backdb = new HashMap<>();
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
            List<Back> backs = backdb.get(p.getUniqueId());
            if (backs == null) backs = new ArrayList<>();
            // remove last location if needed
            if (backs.size() > 0 && backs.size() >= maxStack) backs.remove(backs.size() - 1);
            backs.add(0, new Back(toAdd));
            backdb.put(p.getUniqueId(), backs);
        }
    }

	@Override
	protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
		if (!(cs instanceof Player)) return new ArrayList<>();
		Player player = (Player) cs;
        synchronized (backdb) {
            List<Back> backs = backdb.get(player.getUniqueId());
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
            final List<Back> backs = backdb.get(p.getUniqueId());
            cs.sendMessage(MessageColor.NEUTRAL + "/back locations:");
            for (int i = 0; i < backs.size(); i++) {
                Back b = backs.get(i);
                if (b == null) continue;
                BaseComponent[] message = new ComponentBuilder("  ")
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/back " + (i + 1)))
                        .append(i + 1 + ": ")
                        .color(MessageColor.NEUTRAL.bc())
                        .append("on ")
                        .color(MessageColor.POSITIVE.bc())
                        .append(b.getBlockName())
                        .color(MessageColor.NEUTRAL.bc())
                        .append(" in ")
                        .color(MessageColor.POSITIVE.bc())
                        .append(b.getBiomeName())
                        .color(MessageColor.NEUTRAL.bc())
                        .append(" (")
                        .color(MessageColor.POSITIVE.bc())
                        .append(b.getWorldName())
                        .color(MessageColor.NEUTRAL.bc())
                        .append(", ")
                        .color(MessageColor.POSITIVE.bc())
                        .append(this.df.format(b.getX()))
                        .color(MessageColor.NEUTRAL.bc())
                        .append(", ")
                        .color(MessageColor.POSITIVE.bc())
                        .append(this.df.format(b.getY()))
                        .color(MessageColor.NEUTRAL.bc())
                        .append(", ")
                        .color(MessageColor.POSITIVE.bc())
                        .append(this.df.format(b.getZ()))
                        .color(MessageColor.NEUTRAL.bc())
                        .append(")")
                        .color(MessageColor.POSITIVE.bc())
                        .create();
                cs.spigot().sendMessage(message);
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
        List<Back> backs = backdb.get(p.getUniqueId());
        if (index < 0 || index >= backs.size()) {
            cs.sendMessage(MessageColor.NEGATIVE + "No such back number!");
            return true;
        }
        final String error = rp.getTeleporter().teleport(backs.get(index).getLoc());
        if (!error.isEmpty()) {
            p.sendMessage(MessageColor.NEGATIVE + error);
            return true;
        }
        p.sendMessage(MessageColor.POSITIVE + "Returning to your previous location.");
        return true;
    }
}
