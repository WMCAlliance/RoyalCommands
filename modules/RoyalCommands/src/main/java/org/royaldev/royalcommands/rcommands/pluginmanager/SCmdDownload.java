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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class SCmdDownload extends SubCommand<CmdPluginManager> {

    public SCmdDownload(final RoyalCommands instance, final CmdPluginManager parent) {
        super(instance, parent, "download", true, "Attempts to download a plugin from BukkitDev using its tag - recursive can be \"true\" if you would like the plugin to search for jars in all subdirectories of an archive downloaded", "<command> [tag] (recursive)", new String[0], new Short[0]);
    }

    @Override
    public boolean runCommand(final CommandSender cs, Command cmd, String label, String[] eargs, CommandArguments ca) {
        if (eargs.length < 1) {
            cs.sendMessage(MessageColor.NEGATIVE + "Please provide plugin tag!");
            cs.sendMessage(MessageColor.NEGATIVE + "http://dev.bukkit.org/server-mods/" + MessageColor.NEUTRAL + "royalcommands" + MessageColor.NEGATIVE + "/");
            return true;
        }
        final boolean recursive = eargs.length > 1 && eargs[1].equalsIgnoreCase("true");
        final String customTag = this.getParent().getCustomTag(eargs[0]);
        final String tag = (customTag == null) ? eargs[0].toLowerCase() : customTag;
        final String commandUsed = label;
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                cs.sendMessage(MessageColor.POSITIVE + "Getting download link...");
                String pluginUrlString = "http://dev.bukkit.org/server-mods/" + tag + "/files.rss";
                String file;
                try {
                    final URL url = new URL(pluginUrlString);
                    final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
                    doc.getDocumentElement().normalize();
                    final NodeList nodes = doc.getElementsByTagName("item");
                    final Node firstNode = nodes.item(0);
                    if (firstNode.getNodeType() == 1) {
                        final Element firstElement = (Element) firstNode;
                        final NodeList firstElementTagName = firstElement.getElementsByTagName("link");
                        final Element firstNameElement = (Element) firstElementTagName.item(0);
                        final NodeList firstNodes = firstNameElement.getChildNodes();
                        final String link = firstNodes.item(0).getNodeValue();
                        final URL dpage = new URL(link);
                        final BufferedReader br = new BufferedReader(new InputStreamReader(dpage.openStream()));
                        final StringBuilder content = new StringBuilder();
                        String inputLine;
                        while ((inputLine = br.readLine()) != null) content.append(inputLine);
                        br.close();
                        file = StringUtils.substringBetween(content.toString(), "<li class=\"user-action user-action-download\"><span><a href=\"", "\">Download</a></span></li>");
                    } else throw new Exception();
                } catch (Exception e) {
                    cs.sendMessage(MessageColor.NEGATIVE + "Could not fetch download link! Either this plugin has no downloads, or you specified an invalid tag.");
                    cs.sendMessage(MessageColor.NEGATIVE + "Tag: http://dev.bukkit.org/server-mods/" + MessageColor.NEUTRAL + "plugin-name" + MessageColor.NEGATIVE + "/");
                    return;
                }
                if (SCmdDownload.this.getParent().downloadAndMovePlugin(file, null, recursive, cs)) {
                    cs.sendMessage(MessageColor.POSITIVE + "Downloaded plugin. Use " + MessageColor.NEUTRAL + "/" + commandUsed + " load" + MessageColor.POSITIVE + " to enable it.");
                } else cs.sendMessage(MessageColor.NEGATIVE + "Could not download that plugin. Please try again.");
            }
        };
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, r);
        return true;
    }
}
