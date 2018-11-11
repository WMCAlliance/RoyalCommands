/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.trade.guiitems;

import java.util.Arrays;
import org.bukkit.Material;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.gui.inventory.GUIItem;
import org.royaldev.royalcommands.rcommands.trade.Party;
import org.royaldev.royalcommands.rcommands.trade.Trade;

public class ToggleAcceptanceItem extends GUIItem {

    public ToggleAcceptanceItem(final Trade trade) {
        super(
            Material.WRITABLE_BOOK,
            MessageColor.RESET + "Toggle Acceptance",
            Arrays.asList(
                MessageColor.NEUTRAL + "This toggles your acceptance of the trade.",
                MessageColor.NEUTRAL + "Once both sides have accepted,",
                MessageColor.NEUTRAL + "the trade will process.",
                trade.getTradeStatusLore(Party.TRADER),
                trade.getTradeStatusLore(Party.TRADEE)
            )
        );
    }
}
