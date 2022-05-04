/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.block.BrewingStand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import java.util.*;

@ReflectCommand
public class CmdBrewingStand extends TabCommand {

    public final Map<UUID, BrewingStand> bewstanddb = new HashMap<>();

    public CmdBrewingStand(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.LIST.getShort()});
    }
	
    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>(Arrays.asList("set", "show"));
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        Player p = (Player) cs;
        String command = args[0].toLowerCase();
        switch (command) {
            case "set": {
                if (!(RUtils.getTarget(p).getState() instanceof BrewingStand)) {
                    cs.sendMessage(MessageColor.NEGATIVE + "That's not a brewing stand!");
                    return true;
                }
                BrewingStand bs = (BrewingStand) RUtils.getTarget(p).getState();
                this.bewstanddb.put(p.getUniqueId(), bs);
                cs.sendMessage(MessageColor.POSITIVE + "Brewing Stand set.");
                return true;
            }
            case "show": {
                if (!this.bewstanddb.containsKey(p.getUniqueId())) {
                    cs.sendMessage(MessageColor.NEGATIVE + "You must first set a brewing stand!");
                    return true;
                }
                BrewingStand bs = this.bewstanddb.get(p.getUniqueId());
                if (!(bs.getBlock().getState() instanceof BrewingStand)) {
                    cs.sendMessage(MessageColor.NEGATIVE + "The brewing stand is no longer there!");
                    return true;
                }
                bs = (BrewingStand) bs.getBlock().getState();
                BrewerInventory bi = bs.getInventory();
                p.openInventory(bi);
                cs.sendMessage(MessageColor.POSITIVE + "Opened your brewing stand for you.");
                return true;
            }
            default:
                cs.sendMessage(MessageColor.NEGATIVE + "Try " + MessageColor.NEUTRAL + "/" + label + MessageColor.POSITIVE + ".");
                return true;
        }
    }
}
