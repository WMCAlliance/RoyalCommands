/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.nms.api;

public interface NMSFace {
    /**
     * Gets the version of the NMS support (e.g. v1_4_5).
     *
     * @return Version string
     */
    public String getVersion();

    /**
     * Checks to see if this is offering NMS/CB internals support.
     * <p/>
     * Note that if this returns false, calling any method besides this and getVersion() will throw an
     * {@link UnsupportedOperationException}.
     * If this is true, NMS support is enabled.
     *
     * @return true/false
     */
    public boolean hasSupport();

}
