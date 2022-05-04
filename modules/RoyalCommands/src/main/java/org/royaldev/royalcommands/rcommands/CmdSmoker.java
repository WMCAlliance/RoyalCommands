/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.block.Smoker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import java.util.*;

@ReflectCommand
public class CmdSmoker extends TabCommand {

    public final Map<UUID, Smoker> smokerdb = new HashMap<>();

    public CmdSmoker(final RoyalCommands instance, final String name) {
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
                if (!(RUtils.getTarget(p).getState() instanceof Smoker)) {
                    cs.sendMessage(MessageColor.NEGATIVE + "That's not a smoker!");
                    return true;
                }
                Smoker s = (Smoker) RUtils.getTarget(p).getState();
                this.smokerdb.put(p.getUniqueId(), s);
                cs.sendMessage(MessageColor.POSITIVE + "Smoker set.");
                return true;
            }
            case "show": {
                if (!this.smokerdb.containsKey(p.getUniqueId())) {
                    cs.sendMessage(MessageColor.NEGATIVE + "You must first set a smoker!");
                    return true;
                }
                Smoker s = this.smokerdb.get(p.getUniqueId());
                if (!(s.getBlock().getState() instanceof Smoker)) {
                    cs.sendMessage(MessageColor.NEGATIVE + "The smoker is no longer there!");
                    return true;
                }
                s = (Smoker) s.getBlock().getState();
                FurnaceInventory fi = s.getInventory();
                p.openInventory(fi);
                cs.sendMessage(MessageColor.POSITIVE + "Opened your smoker for you.");
                return true;
            }
            default:
                cs.sendMessage(MessageColor.NEGATIVE + "Try " + MessageColor.NEUTRAL + "/" + label + MessageColor.POSITIVE + ".");
                return true;
        }
    }
}
