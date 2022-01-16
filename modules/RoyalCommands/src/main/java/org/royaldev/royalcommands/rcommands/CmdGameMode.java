/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AuthorizationHandler.PermType;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdGameMode extends TabCommand {

    public CmdGameMode(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort(), CompletionType.ENUM.getShort()});
    }

    @Override
    protected Enum[] customEnum(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return GameMode.values();
    }

    /**
     * Gets a GameMode from a string. Can be in word format ("creative") or number format ("1").
     *
     * @param s String representing GameMode
     * @return GameMode
     */
    public GameMode getGameMode(String s) {
        if (s == null) return null;
        GameMode toRet;
        try {
            toRet = GameMode.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                Integer i = Integer.valueOf(s);
                switch (i) {
                    case 0:
                        toRet = GameMode.SURVIVAL;
                        break;
                    case 1:
                        toRet = GameMode.CREATIVE;
                        break;
                    case 2:
                        toRet = GameMode.ADVENTURE;
                        break;
                    case 3:
                        toRet = GameMode.SPECTATOR;
                        break;
                    default:
                        toRet = null;
                }
            } catch (NumberFormatException e2) {
                return null;
            }
        }
        return toRet;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player) && args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        if (args.length < 1) {
            Player p = (Player) cs;
            GameMode toSet = (p.getGameMode().equals(GameMode.CREATIVE)) ? GameMode.SURVIVAL : GameMode.CREATIVE;
            p.setGameMode(toSet);
            p.sendMessage(MessageColor.POSITIVE + "Your game mode has been set to " + MessageColor.NEUTRAL + toSet.name().toLowerCase() + MessageColor.POSITIVE + ".");
            return true;
        }
        if (args.length > 0) {
            Player t = this.plugin.getServer().getPlayer(args[0]);
            if (getGameMode(args[0]) != null) {
                assert cs instanceof Player;
                Player p = (Player) cs;
                GameMode toSet = (p.getGameMode().equals(GameMode.CREATIVE)) ? GameMode.SURVIVAL : GameMode.CREATIVE;
                GameMode result = getGameMode(args[0]);
                if (result == null) {
                    cs.sendMessage(MessageColor.NEGATIVE + "Invalid gamemode!");
                    cs.sendMessage(MessageColor.NEUTRAL + RUtils.join(GameMode.values(), MessageColor.RESET + ", " + MessageColor.NEUTRAL));
                    return true;
                }
                toSet = result;
                p.setGameMode(result);
                p.sendMessage(MessageColor.POSITIVE + "Your game mode has been changed to " + MessageColor.NEUTRAL + toSet.name().toLowerCase() + MessageColor.POSITIVE + ".");
                return true;
            }
            if (t == null || this.plugin.isVanished(t, cs)) {
                cs.sendMessage(MessageColor.NEGATIVE + "That player does not exist!");
                return true;
            }
            if (!t.equals(cs) && !this.ah.isAuthorized(cs, cmd, PermType.OTHERS)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You can't change other players' gamemodes!");
                return true;
            }
            if (!t.equals(cs) && this.ah.isAuthorized(t, cmd, PermType.EXEMPT)) {
                cs.sendMessage(MessageColor.NEGATIVE + "You cannot change that player's gamemode.");
                return true;
            }
            GameMode toSet = (t.getGameMode().equals(GameMode.CREATIVE)) ? GameMode.SURVIVAL : GameMode.CREATIVE;
            if (args.length > 1) {
                GameMode result = getGameMode(args[1]);
                if (result == null) {
                    cs.sendMessage(MessageColor.NEGATIVE + "Invalid gamemode!");
                    cs.sendMessage(MessageColor.NEUTRAL + RUtils.join(GameMode.values(), MessageColor.RESET + ", " + MessageColor.NEUTRAL));
                    return true;
                }
                toSet = result;
            }
            t.setGameMode(toSet);
            if (cs instanceof Player && !cs.equals(t))
                cs.sendMessage(MessageColor.POSITIVE + "You have changed " + MessageColor.NEUTRAL + t.getName() + "\'s" + MessageColor.POSITIVE + " game mode to " + MessageColor.NEUTRAL + toSet.name().toLowerCase() + MessageColor.POSITIVE + ".");
            t.sendMessage(MessageColor.POSITIVE + "Your game mode has been changed to " + MessageColor.NEUTRAL + toSet.name().toLowerCase() + MessageColor.POSITIVE + ".");
            return true;
        }
        return true;
    }
}
