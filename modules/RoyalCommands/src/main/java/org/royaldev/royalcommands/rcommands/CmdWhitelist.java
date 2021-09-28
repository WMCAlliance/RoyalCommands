/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.whitelist.SCmdAdd;
import org.royaldev.royalcommands.rcommands.whitelist.SCmdCheck;
import org.royaldev.royalcommands.rcommands.whitelist.SCmdRemove;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import java.util.UUID;

@ReflectCommand
public class CmdWhitelist extends ParentCommand {

    public static final Flag<String> PLAYER_FLAG = new Flag<>(String.class, "player", "p", "name", "n");
    public static final Flag<String> UUID_FLAG = new Flag<>(String.class, "uuid", "u");

    public CmdWhitelist(final RoyalCommands instance, final String name) {
        super(instance, name, false);
        addExpectedFlag(UUID_FLAG);
        addExpectedFlag(PLAYER_FLAG);
        this.addSubCommand(new SCmdAdd(this.plugin, this));
        this.addSubCommand(new SCmdCheck(this.plugin, this));
        this.addSubCommand(new SCmdRemove(this.plugin, this));
    }

    @Override
    protected void addSubCommand(final SubCommand sc) {
        super.addSubCommand(sc);
    }

    public RPlayer getRPlayer(final CommandArguments ca, final CommandSender cs) {
        final RPlayer rp;
        if (ca.hasContentFlag(CmdWhitelist.UUID_FLAG)) {
            try {
                final UUID uuid = UUID.fromString(ca.getFlag(CmdWhitelist.UUID_FLAG).getValue());
                rp = MemoryRPlayer.getRPlayer(uuid);
            } catch (final IllegalArgumentException ex) {
                cs.sendMessage(MessageColor.NEGATIVE + "Invalid UUID format!");
                return null;
            }
        } else if (ca.hasContentFlag(CmdWhitelist.PLAYER_FLAG)) {
            rp = MemoryRPlayer.getRPlayer(ca.getFlag(CmdWhitelist.PLAYER_FLAG).getValue());
        } else {
            rp = null;
        }
        return rp;
    }

    public void reloadWhitelist() {
        Config.whitelist = this.plugin.whl.getStringList("whitelist");
    }
}
