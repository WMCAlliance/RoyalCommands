/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.OfflinePlayer;

public class PlaceholderHandler extends PlaceholderExpansion {
    private final RoyalCommands plugin;
    private final String prefix = "royalcommands";

    private final List<String> placeholders = Arrays.asList(
        "afk"
    );

    public PlaceholderHandler(RoyalCommands instance) {
        this.plugin = instance;
    }

    @Override
    public String getAuthor() {
        return this.plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return prefix;
    }

    @Override
    public String getVersion() {
        return this.plugin.version;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public List<String> getPlaceholders() {
        List<String> ph = new ArrayList<>();

        for (String pl : placeholders) {
            ph.add("%" + prefix + "_" + pl + "%");
        }

        return ph;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("afk")) {
            return AFKUtils.isAfk(player.getPlayer()) ? Config.afkOnPlaceholder : Config.afkOffPlaceholder;
        }

        return null;
    }
}
