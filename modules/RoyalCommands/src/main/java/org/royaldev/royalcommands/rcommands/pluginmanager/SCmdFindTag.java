/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.pluginmanager;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdPluginManager;
import org.royaldev.royalcommands.rcommands.SubCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SCmdFindTag extends SubCommand<CmdPluginManager> {

    public SCmdFindTag(final RoyalCommands instance, final CmdPluginManager parent) {
        super(instance, parent, "findtag", true, "Searches BukkitDev for a tag to use in download", "<command> [search] (page)", new String[]{"search"}, new Short[0]);
    }

    @Override
    public boolean runCommand(final CommandSender cs, Command cmd, String label, String[] eargs, CommandArguments ca) {
        if (eargs.length < 1) {
            cs.sendMessage(MessageColor.NEGATIVE + "Please specify a search term!");
            return true;
        }
        int page = 1;
        if (eargs.length > 1) {
            try {
                page = Integer.parseInt(eargs[eargs.length - 1]);
            } catch (NumberFormatException ignored) {
                page = 1;
            }
        }
        final String search;
        try {
            search = URLEncoder.encode(StringUtils.join(eargs, " "), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Tell the developer enc1.");
            return true;
        }
        final URL u;
        try {
            u = new URL("http://dev.bukkit.org/search/?scope=projects&search=" + search + "&page=" + page);
        } catch (MalformedURLException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Malformed search term!");
            return true;
        }
        final BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(u.openStream()));
        } catch (IOException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Internal input/output error. Please try again.");
            return true;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                String inputLine;
                StringBuilder content = new StringBuilder();
                try {
                    while ((inputLine = br.readLine()) != null) content.append(inputLine);
                } catch (IOException e) {
                    cs.sendMessage(MessageColor.NEGATIVE + "Internal input/output error. Please try again.");
                    return;
                }
                cs.sendMessage(MessageColor.POSITIVE + "Project name" + MessageColor.NEUTRAL + " - tag");
                for (int i = 0; i < 20; i++) {
                    final String project = StringUtils.substringBetween(content.toString(), " row-joined-to-next\">", "</tr>");
                    final String base = StringUtils.substringBetween(project, "<td class=\"col-search-entry\">", "</td>");
                    if (base == null) {
                        if (i == 0) cs.sendMessage(MessageColor.NEGATIVE + "No results found.");
                        return;
                    }
                    final Pattern p = Pattern.compile("<h2><a href=\"/bukkit-plugins/([\\W\\w]+)/\">([\\w\\W]+)</a></h2>");
                    final Matcher m = p.matcher(base);
                    if (!m.find()) {
                        if (i == 0) cs.sendMessage(MessageColor.NEGATIVE + "No results found.");
                        return;
                    }
                    final String name = m.group(2).replaceAll("</?\\w+>", "");
                    final String tag = m.group(1);
                    final int beglen = StringUtils.substringBefore(content.toString(), base).length();
                    content = new StringBuilder(content.substring(beglen + project.length()));
                    cs.sendMessage(MessageColor.POSITIVE + name + MessageColor.NEUTRAL + " - " + tag);
                }
            }
        };
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, r);
        return true;
    }
}
