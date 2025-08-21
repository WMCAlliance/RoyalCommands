/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.home;

import org.bukkit.Location;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.configuration.PlayerConfiguration;
import org.royaldev.royalcommands.configuration.PlayerConfigurationManager;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import java.util.UUID;

public class Home {

    private final UUID uuid;
    private final String name;
    private Location location;

    public Home(final UUID uuid, final String name, final Location location) {
        this.uuid = uuid;
        this.name = name;
        this.location = location;
    }

    public static Home createEmptyFromNotation(final UUID uuid, final String notation) {
        final String[] parts = notation.split(":");
        final String playerName = parts.length > 1 ? parts[0] : null;
        final String homeName = parts[parts.length > 1 ? 1 : 0];
        final PlayerConfiguration pcm;
        if (playerName == null) pcm = PlayerConfigurationManager.getConfiguration(uuid);
        else
            pcm = PlayerConfigurationManager.getConfiguration(RUtils.getOfflinePlayer(playerName));
        return new Home(pcm.getManagerPlayerUUID(), homeName, null);
    }

    /**
     * @param uuid     UUID of requester
     * @param notation Notation used
     * @return Home
     */
    public static Home fromNotation(final UUID uuid, final String notation) {
        final String[] parts = notation.split(":");
        final String playerName = parts.length > 1 ? parts[0] : null;
        final String homeName = parts[parts.length > 1 ? 1 : 0];
        final PlayerConfiguration pcm;
        if (playerName == null) pcm = PlayerConfigurationManager.getConfiguration(uuid);
        else
            pcm = PlayerConfigurationManager.getConfiguration(RUtils.getOfflinePlayer(playerName));
        return Home.fromPConfManager(pcm, homeName);
    }

    public static Home fromPConfManager(final PlayerConfiguration pcm, final String name) {
        if (!pcm.isSet("home." + name)) return null;
        try {
            return new Home(pcm.getManagerPlayerUUID(), name, pcm.getLocation("home." + name));
        } catch (final Exception ex) {
            return null;
        }
    }

    private String getPath() {
        return "home." + this.getName();
    }

    public void delete() {
        final PlayerConfiguration pcm = this.getRPlayer().getPlayerConfiguration();
        pcm.set(this.getPath(), null);
        pcm.forceSave();
    }

    public String getFullName() {
        return this.getRPlayer().getName() + ":" + this.getName();
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public String getName() {
        return this.name;
    }

    public RPlayer getRPlayer() {
        return MemoryRPlayer.getRPlayer(this.uuid);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void save() {
        final PlayerConfiguration pcm = this.getRPlayer().getPlayerConfiguration();
        pcm.setLocation(this.getPath(), this.getLocation());
        pcm.forceSave();
    }

    @Override
    public String toString() {
        return String.format(
            "Home[uuid=%s, name=%s, location=%s]",
            this.uuid,
            this.name,
            this.location
        );
    }
}
