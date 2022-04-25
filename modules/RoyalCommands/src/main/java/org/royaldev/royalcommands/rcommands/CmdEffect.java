/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdEffect extends TabCommand {

    public CmdEffect(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort(), CompletionType.EFFECT.getShort()});
    }

    private void sendPotionTypes(CommandSender cs) {
        StringBuilder sb = new StringBuilder();
        for (PotionEffectType pet : PotionEffectType.values()) {
            if (pet == null) continue;
            sb.append(MessageColor.NEUTRAL);
            sb.append(pet.getName());
            sb.append(MessageColor.RESET);
            sb.append(", ");
        }
        cs.sendMessage(sb.substring(0, sb.length() - 4)); // "&r, " = 4
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, String[] args, CommandArguments ca) {
        if (args.length < 2) {
            sendPotionTypes(cs);
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        Player t = this.plugin.getServer().getPlayer(args[0]);
        if (t == null || this.plugin.isVanished(t, cs)) {
            cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
            return true;
        }
        if (!t.getName().equals(cs.getName()) && !this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You cannot apply effects to other players!");
            return true;
        }
        if (!t.getName().equals(cs.getName()) && this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
            cs.sendMessage(MessageColor.NEGATIVE + "You cannot apply effects to that player!");
            return true;
        }
        args = (String[]) ArrayUtils.subarray(args, 1, args.length);
        for (String arg : args) {
            if (arg.equalsIgnoreCase("remove") || arg.equalsIgnoreCase("clear")) {
                for (PotionEffect pe : t.getActivePotionEffects()) t.removePotionEffect(pe.getType());
                cs.sendMessage(MessageColor.POSITIVE + "Potion effects removed.");
                return true;
            }
            String[] parts = arg.split(",");
            if (parts.length < 3) {
                cs.sendMessage(MessageColor.NEUTRAL + arg + MessageColor.NEGATIVE + ": Not a complete effect! (name,duration (sec),amplifier(,ambient)).");
                continue;
            }
            String name = parts[0];
            PotionEffectType pet = PotionEffectType.getByName(name.toUpperCase());
            if (pet == null) {
                sendPotionTypes(cs);
                cs.sendMessage(MessageColor.NEUTRAL + arg + MessageColor.NEGATIVE + ": No such potion effect type!");
                continue;
            }
            int duration;
            int amplifier;
            boolean ambient = false;
            try {
                duration = Integer.parseInt(parts[1]);
                amplifier = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "Duration/amplifier was not a number!");
                continue;
            }
            if (parts.length > 3) ambient = parts[3].equalsIgnoreCase("true");
            new PotionEffect(pet, duration*20, amplifier, ambient).apply(t);
            cs.sendMessage(MessageColor.POSITIVE + "Applied effect to " + MessageColor.NEUTRAL + t.getName() + MessageColor.POSITIVE + ".");
            return true;
        }
        return true;
    }
}
