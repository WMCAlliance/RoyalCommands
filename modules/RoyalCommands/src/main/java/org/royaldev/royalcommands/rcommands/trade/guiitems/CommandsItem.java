/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.trade.guiitems;

import java.util.List;
import org.bukkit.Material;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.gui.inventory.GUIItem;

public class CommandsItem extends GUIItem {

    public CommandsItem(final List<String> commands) {
        super(
            Material.ENCHANTING_TABLE,
            MessageColor.RESET + "Commands",
            commands
        );
    }
}
