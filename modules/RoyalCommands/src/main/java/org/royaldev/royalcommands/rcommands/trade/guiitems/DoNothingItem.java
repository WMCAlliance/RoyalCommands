/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.trade.guiitems;

import org.bukkit.Material;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.gui.inventory.GUIItem;

public class DoNothingItem extends GUIItem {

    public DoNothingItem() {
        super(
            Material.BARRIER,
            MessageColor.RESET + "",
            null
        );
    }
}
