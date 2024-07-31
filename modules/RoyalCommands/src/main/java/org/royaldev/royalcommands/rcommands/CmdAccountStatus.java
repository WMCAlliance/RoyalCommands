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
import org.royaldev.royalcommands.shaded.mkremins.fanciful.FancyMessage;

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
        // @formatter:off
        new FancyMessage(name)
                .color(MessageColor.NEUTRAL.cc())
                .formattedTooltip(RUtils.getPlayerTooltip(p))
            .then(" has ")
                .color(MessageColor.POSITIVE.cc())
            .then(isPremium ? "paid" : "not paid")
                .color(MessageColor.NEUTRAL.cc())
            .then(" for Minecraft.")
                .color(MessageColor.POSITIVE.cc())
            .send(cs);
        // @formatter:on
        return true;
    }

}
