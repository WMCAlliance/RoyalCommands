/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.nms.NoSupport;

import org.royaldev.royalcommands.nms.api.NMSFace;

public class NMSHandler implements NMSFace {
    @Override
    public String getVersion() {
        return "NoSupport";
    }

    @Override
    public boolean hasSupport() {
        return false;
    }

}
