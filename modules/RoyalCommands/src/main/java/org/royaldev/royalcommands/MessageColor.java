/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands;

import org.bukkit.ChatColor;

public enum MessageColor {

    NEGATIVE(ChatColor.RED),
    NEUTRAL(ChatColor.GRAY),
    POSITIVE(ChatColor.BLUE),
    RESET(ChatColor.RESET);

    private final ChatColor def;

    MessageColor(final ChatColor def) {
        this.def = def;
    }

    public ChatColor cc() {
        return this.getChatColor();
    }

    protected ChatColor byStringOrDefault(final String s) {
        try {
            return ChatColor.valueOf(s.toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return this.def;
        }
    }

    public ChatColor getChatColor() {
        final String s;
		s = switch (this) {
			case NEGATIVE -> Config.negativeChatColor;
			case NEUTRAL -> Config.neutralChatColor;
			case POSITIVE -> Config.positiveChatColor;
			case RESET -> Config.resetChatColor;
			default -> null;
		};
        return this.byStringOrDefault(s);
    }

    @Override
    public String toString() {
        return this.getChatColor().toString();
    }

}
