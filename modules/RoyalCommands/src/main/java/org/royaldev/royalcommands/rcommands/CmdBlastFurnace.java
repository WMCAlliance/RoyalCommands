/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.block.BlastFurnace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import java.util.*;

@ReflectCommand
public class CmdBlastFurnace extends TabCommand {

    public final Map<UUID, BlastFurnace> furnacedb = new HashMap<>();

    public CmdBlastFurnace(final RoyalCommands instance, final String name) {
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
                if (!(RUtils.getTarget(p).getState() instanceof BlastFurnace)) {
                    cs.sendMessage(MessageColor.NEGATIVE + "That's not a blast furnace!");
                    return true;
                }
                BlastFurnace f = (BlastFurnace) RUtils.getTarget(p).getState();
                this.furnacedb.put(p.getUniqueId(), f);
                cs.sendMessage(MessageColor.POSITIVE + "Blast Furnace set.");
                return true;
            }
            case "show": {
                if (!this.furnacedb.containsKey(p.getUniqueId())) {
                    cs.sendMessage(MessageColor.NEGATIVE + "You must first set a blast furnace!");
                    return true;
                }
                BlastFurnace f = this.furnacedb.get(p.getUniqueId());
                if (!(f.getBlock().getState() instanceof BlastFurnace)) {
                    cs.sendMessage(MessageColor.NEGATIVE + "The blast furnace is no longer there!");
                    return true;
                }
                f = (BlastFurnace) f.getBlock().getState();
                FurnaceInventory fi = f.getInventory();
                p.openInventory(fi);
                cs.sendMessage(MessageColor.POSITIVE + "Opened your blast furnace for you.");
                return true;
            }
            default:
                cs.sendMessage(MessageColor.NEGATIVE + "Try " + MessageColor.NEUTRAL + "/" + label + MessageColor.POSITIVE + ".");
                return true;
        }
    }
}
