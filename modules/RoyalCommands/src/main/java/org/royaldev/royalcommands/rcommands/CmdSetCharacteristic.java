/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdSetCharacteristic extends TabCommand {

    public CmdSetCharacteristic(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort(), CompletionType.LIST.getShort(), CompletionType.CUSTOM.getShort()});
    }
	
    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>(Arrays.asList("maxhealth", "maxair", "exp", "canpickupitems"));
    }
	
	@Override
	protected List<String> getCustomCompletions(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
		switch(args[1].toLowerCase()) {
			case "canpickupitems":
				return new ArrayList<>(Arrays.asList("true", "false"));
		}
		return new ArrayList<>();
	}
	
    private Float toFloat(Object o) {
        try {
            return Float.parseFloat(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInt(Object o) {
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            cs.sendMessage(MessageColor.POSITIVE + "/" + label + " help:");
            cs.sendMessage(MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " [player] maxhealth [half-hearts]");
            cs.sendMessage(MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " [player] maxair [ticks]");
            cs.sendMessage(MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " [player] exp [percentage]");
            cs.sendMessage(MessageColor.POSITIVE + "/" + label + MessageColor.NEUTRAL + " [player] canpickupitems [boolean]");
            return true;
        }
        if (args.length < 3) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        Player p = this.plugin.getServer().getPlayer(args[0]);
        if (p == null || this.plugin.isVanished(p, cs)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        String subcommand = args[1];
        if (subcommand.equalsIgnoreCase("maxhealth")) {
            Integer i = toInt(args[2]);
            if (i == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "The max health was not a number!");
                return true;
            }
            if (i < 1) {
                cs.sendMessage(MessageColor.NEGATIVE + "Cannot set maxhealth to less than 1.");
                return true;
            }
            p.setMaxHealth(i);
            cs.sendMessage(MessageColor.POSITIVE + "Set max health to " + MessageColor.NEUTRAL + i + MessageColor.POSITIVE + ".");
        } else if (subcommand.equalsIgnoreCase("maxair")) {
            Integer i = toInt(args[2]);
            if (i == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "The max air was not a number!");
                return true;
            }
            p.setMaximumAir(i);
            cs.sendMessage(MessageColor.POSITIVE + "Set max air to " + MessageColor.NEUTRAL + i + MessageColor.POSITIVE + ".");
        } else if (subcommand.equalsIgnoreCase("exp")) {
            Float f = toFloat(args[2]);
            if (f == null) {
                cs.sendMessage(MessageColor.NEGATIVE + "The exp was not a number!");
                return true;
            }
            f /= 100F;
            if (f < 0F || f > 1F) {
                cs.sendMessage(MessageColor.NEGATIVE + "Exp must be a percentage between 0 and 100.");
                return true;
            }
            p.setExp(f);
            cs.sendMessage(MessageColor.POSITIVE + "Set exp to " + MessageColor.NEUTRAL + (f * 100F) + "%" + MessageColor.POSITIVE + ".");
        } else if (subcommand.equalsIgnoreCase("canpickupitems")) {
            p.setCanPickupItems(args[2].equalsIgnoreCase("true"));
            cs.sendMessage(MessageColor.POSITIVE + "Set can pick up items to " + MessageColor.NEUTRAL + Boolean.toString(p.getCanPickupItems()) + MessageColor.POSITIVE + ".");
        } else cs.sendMessage(MessageColor.NEGATIVE + "No such subcommand!");
        return true;
    }
}
