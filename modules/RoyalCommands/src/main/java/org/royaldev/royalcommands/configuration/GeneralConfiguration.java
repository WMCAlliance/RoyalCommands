/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.configuration;

import org.bukkit.Location;
import org.bukkit.configuration.Configuration;

public interface GeneralConfiguration extends Configuration {

    float getFloat(String path);

    Location getLocation(String path, String worldName);

    Location getLocation(String path);

    void setLocation(String path, Location value);
}
