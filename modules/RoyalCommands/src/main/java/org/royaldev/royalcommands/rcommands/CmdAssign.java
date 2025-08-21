/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;

@ReflectCommand
public class CmdAssign extends TabCommand {

    public CmdAssign(final RoyalCommands instance, final String name) {
        super(instance, name, true,new Short[]{CompletionType.LIST.getShort(), CompletionType.ANY_COMMAND.getShort()});
    }
    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>(Arrays.asList("add", "remove", "list"));
    }
    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        final Player p = (Player) cs;
        final PlayerConfiguration pcm = PlayerConfigurationManager.getConfiguration(p);
        final ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't modify commands on air!");
            return true;
        }
        List<String> cmds = RUtils.getAssignment(hand, pcm);
        if (cmds == null) cmds = new ArrayList<>();
        if (eargs.length < 1) {
            RUtils.removeAssignment(hand, PlayerConfigurationManager.getConfiguration(p));
            p.sendMessage(MessageColor.POSITIVE + "All commands removed from " + MessageColor.NEUTRAL + RUtils.getItemName(hand) + MessageColor.POSITIVE + ".");
            return true;
        }
        switch (eargs[0].toLowerCase()) {
            case "remove":
                int toRemove = Integer.parseInt(eargs[1]);
                if (toRemove <= 0 || toRemove > cmds.size()) {
                    cs.sendMessage(MessageColor.NEGATIVE + "The number specified does not exist!");
                    return true;
                }
                toRemove--;
                cmds.remove(toRemove);
                RUtils.setAssignment(hand, cmds, pcm);
                cs.sendMessage(MessageColor.POSITIVE + "Removed command " + MessageColor.NEUTRAL + (toRemove + 1) + MessageColor.POSITIVE + ".");
                return true;
            case "list":
                cs.sendMessage(MessageColor.POSITIVE + "Commands on " + MessageColor.NEUTRAL + RUtils.getItemName(hand) + MessageColor.POSITIVE + ":");
                if (cmds.isEmpty()) {
                    cs.sendMessage("  " + MessageColor.NEUTRAL + "None.");
                    return true;
                }
                for (int i = 0; i < cmds.size(); i++) {
                    cs.sendMessage("  " + MessageColor.NEUTRAL + (i + 1) + MessageColor.POSITIVE + ": " + MessageColor.NEUTRAL + cmds.get(i));
                }
                return true;
            case "add":
                cmds.add(eargs[1]);
                RUtils.setAssignment(hand, cmds, pcm);
                String message = (eargs[1].toLowerCase().startsWith("c:")) ? MessageColor.POSITIVE + "Added message " + MessageColor.NEUTRAL + eargs[1].replace("c:", "") + MessageColor.POSITIVE + " to that item." : MessageColor.POSITIVE + "Added command " + MessageColor.NEUTRAL + "/" + eargs[1] + MessageColor.POSITIVE + " to that item.";
                p.sendMessage(message);
                return true;
            default:
                cs.sendMessage(cmd.getDescription());
                return false;
        }

    }
}
