/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.whitelist.SCmdAdd;
import org.royaldev.royalcommands.rcommands.whitelist.SCmdCheck;
import org.royaldev.royalcommands.rcommands.whitelist.SCmdRemove;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@ReflectCommand
public class CmdWhitelist extends ParentCommand {

    public CmdWhitelist(final RoyalCommands instance, final String name) {
        super(instance, name, false);
        this.addSubCommand(new SCmdAdd(this.plugin, this));
        this.addSubCommand(new SCmdCheck(this.plugin, this));
        this.addSubCommand(new SCmdRemove(this.plugin, this));
    }

    @Override
    protected void addSubCommand(final SubCommand sc) {
        super.addSubCommand(sc);
    }

    public RPlayer getRPlayer(String player) {
        final RPlayer rp;
        if (player.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")) {
            final UUID uuid = UUID.fromString(player);
            rp = MemoryRPlayer.getRPlayer(uuid);
        } else {
            rp = MemoryRPlayer.getRPlayer(player);
        }
        return rp;
    }

    public void reloadWhitelist() {
        Config.whitelist = this.plugin.whl.getStringList("whitelist");
    }
}
