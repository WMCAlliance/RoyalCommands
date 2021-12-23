/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.worldmanager.SCmdCreate;
import org.royaldev.royalcommands.rcommands.worldmanager.SCmdDelete;
import org.royaldev.royalcommands.rcommands.worldmanager.SCmdInfo;
import org.royaldev.royalcommands.rcommands.worldmanager.SCmdList;
import org.royaldev.royalcommands.rcommands.worldmanager.SCmdLoad;
import org.royaldev.royalcommands.rcommands.worldmanager.SCmdTeleport;
import org.royaldev.royalcommands.rcommands.worldmanager.SCmdUnload;
import org.royaldev.royalcommands.rcommands.worldmanager.SCmdWho;

import java.util.Random;

@ReflectCommand
public class CmdWorldManager extends ParentCommand {

    private final Random r = new Random();
    private final Flag<String> nameFlag = new Flag<>(String.class, "name", "n");
    private final Flag ejectFlag = new Flag("eject", "e");

    public CmdWorldManager(final RoyalCommands instance, final String name) {
        super(instance, name, false);
        addExpectedFlag(nameFlag);
        addExpectedFlag(ejectFlag);
        this.addSubCommand(new SCmdCreate(this.plugin, this));
        this.addSubCommand(new SCmdDelete(this.plugin, this));
        this.addSubCommand(new SCmdInfo(this.plugin, this));
        this.addSubCommand(new SCmdList(this.plugin, this));
        this.addSubCommand(new SCmdLoad(this.plugin, this));
        this.addSubCommand(new SCmdTeleport(this.plugin, this));
        this.addSubCommand(new SCmdUnload(this.plugin, this));
        this.addSubCommand(new SCmdWho(this.plugin, this));

    }

    public Random getRandom() {
        return this.r;
    }
}
