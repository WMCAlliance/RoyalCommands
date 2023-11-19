/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.trade.clickhandlers;

import org.royaldev.royalcommands.gui.inventory.ClickEvent;
import org.royaldev.royalcommands.gui.inventory.ClickHandler;

public class DoNothing implements ClickHandler {

    public DoNothing() {}

    @Override
    public boolean onClick(final ClickEvent clickEvent) {
        return false;
    }
}
