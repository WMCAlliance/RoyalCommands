/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.configuration;

import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerConfigurationManager {

    static final Map<UUID, PlayerConfiguration> pcs = Collections.synchronizedMap(new HashMap<>());

    public static int configurationsCreated() {
        synchronized (PlayerConfigurationManager.pcs) {
            return PlayerConfigurationManager.pcs.size();
        }
    }

    public static void discard(final PlayerConfiguration pc) {
        synchronized (PlayerConfigurationManager.pcs) {
            PlayerConfigurationManager.pcs.remove(pc.getManagerPlayerUUID());
        }
    }

    public static synchronized Collection<PlayerConfiguration> getAllConfigurations() {
        synchronized (PlayerConfigurationManager.pcs) {
            return Collections.synchronizedCollection(PlayerConfigurationManager.pcs.values());
        }
    }

    public static PlayerConfiguration getConfiguration(final UUID u) {
        synchronized (PlayerConfigurationManager.pcs) {
            if (PlayerConfigurationManager.pcs.containsKey(u)) return PlayerConfigurationManager.pcs.get(u);
            final PlayerConfiguration pcm = new FilePlayerConfiguration(u);
            PlayerConfigurationManager.pcs.put(u, pcm);
            return pcm;
        }
    }

    public static PlayerConfiguration getConfiguration(final OfflinePlayer p) {
        return PlayerConfigurationManager.getConfiguration(p.getUniqueId());
    }

    public static boolean isConfigurationCreated(final UUID u) {
        synchronized (PlayerConfigurationManager.pcs) {
            return PlayerConfigurationManager.pcs.containsKey(u);
        }
    }

    public static boolean isConfigurationCreated(final OfflinePlayer p) {
        return PlayerConfigurationManager.isConfigurationCreated(p.getUniqueId());
    }

    public static void removeAllConfigurations() {
        final Collection<PlayerConfiguration> oldConfs = new ArrayList<>();
        synchronized (PlayerConfigurationManager.pcs) {
            oldConfs.addAll(PlayerConfigurationManager.pcs.values());
            for (final PlayerConfiguration pcm : oldConfs) pcm.discard(false);
        }
    }

    public static void saveAllConfigurations() {
        synchronized (PlayerConfigurationManager.pcs) {
            for (final PlayerConfiguration pcm : PlayerConfigurationManager.pcs.values()) pcm.forceSave();
        }
    }
}
