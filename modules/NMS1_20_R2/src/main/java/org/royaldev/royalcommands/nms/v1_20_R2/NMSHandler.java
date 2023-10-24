/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.nms.v1_20_R2;

import org.royaldev.royalcommands.nms.api.NMSFace;

public class NMSHandler implements NMSFace {

    @Override
    public String getVersion() {
        return "v1_20_R2";
    }

    @Override
    public boolean hasSupport() {
        return true;
    }

}
