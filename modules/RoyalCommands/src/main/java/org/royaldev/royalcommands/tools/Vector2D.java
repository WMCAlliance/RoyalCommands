/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.tools;

/**
 * A two-dimensional vector.
 */
public class Vector2D {

    private final int x, y;

    public Vector2D(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the X-coordinate.
     *
     * @return int
     */
    public int getX() {
        return this.x;
    }

    /**
     * Gets the Y-coordinate.
     *
     * @return int
     */
    public int getY() {
        return this.y;
    }
}
