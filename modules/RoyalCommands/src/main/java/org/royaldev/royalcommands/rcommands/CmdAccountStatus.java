/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@ReflectCommand
public class CmdAccountStatus extends TabCommand {

    public CmdAccountStatus(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ONLINE_PLAYER.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        String name = args[0];
        final OfflinePlayer p = RUtils.getOfflinePlayer(name);
        name = p.getName();
        URL u;
        try {
            u = new URI("https://api.mojang.com/users/profiles/minecraft/" + URLEncoder.encode(name, StandardCharsets.UTF_8)).toURL();
        } catch (final MalformedURLException ex) {
            cs.sendMessage(MessageColor.NEGATIVE + "An unthinkable error happened. Please let the developer know.");
            return true;
        } catch (final URISyntaxException ex) {
            cs.sendMessage(MessageColor.NEGATIVE + "The UTF-8 encoding is not supported on this system!");
            return true;
        }
        boolean isPremium;
        try {
            HttpURLConnection connection = (HttpURLConnection)u.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            switch(connection.getResponseCode()) {
                case 200:
                    isPremium = true;
                    break;
                case 204:
                    isPremium = false;
                    break;
                default:
                    cs.sendMessage(MessageColor.NEGATIVE + "Could not read from Minecraft's servers!");
                    return true;
            }
        } catch(final IOException ex) {
            cs.sendMessage(MessageColor.NEGATIVE + "Could not read from Minecraft's servers!");
            cs.sendMessage(MessageColor.NEGATIVE + ex.getMessage());
            return true;
        }

        TextComponent tc = new TextComponent();

        TextComponent neutral = new TextComponent(name);
        neutral.setColor(MessageColor.NEUTRAL.bc());
        neutral.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RUtils.getPlayerTooltip(p)));
        tc.addExtra(neutral.duplicate());

        TextComponent positive = new TextComponent(" has ");
        positive.setColor(MessageColor.POSITIVE.bc());
        tc.addExtra(positive.duplicate());

        neutral.setText(isPremium ? "paid" : "not paid");
        neutral.setHoverEvent((HoverEvent) null);
        tc.addExtra(neutral.duplicate());

        positive.setText(" for Minecraft.");
        tc.addExtra(positive.duplicate());

        cs.spigot().sendMessage(tc);
        return true;
    }

}
