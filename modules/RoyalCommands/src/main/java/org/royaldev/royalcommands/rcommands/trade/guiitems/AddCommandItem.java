/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.trade.guiitems;

import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.gui.inventory.GUIItem;
import org.royaldev.royalcommands.rcommands.trade.Party;
import org.royaldev.royalcommands.rcommands.trade.Trade;

public class AddCommandItem extends GUIItem {

    public AddCommandItem(final Trade trade, final Party party) {
        super(
            Material.COMMAND_BLOCK,
            MessageColor.RESET + "Add " + StringUtils.capitalize(party.name().toLowerCase()) + " Command",
            Arrays.asList(
                MessageColor.NEUTRAL + "Adds a command for the " + party.name().toLowerCase(),
                MessageColor.NEUTRAL + "to perform when the trade processes.",
                MessageColor.NEUTRAL + "The " + party.name().toLowerCase() + " is " + trade.getName(party) + "."
            )
        );
    }
}
