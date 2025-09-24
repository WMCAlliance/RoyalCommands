/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.vip.SCmdAdd;
import org.royaldev.royalcommands.rcommands.vip.SCmdCheck;
import org.royaldev.royalcommands.rcommands.vip.SCmdRemove;

@ReflectCommand
public class CmdVip extends ParentCommand {

    public CmdVip(final RoyalCommands instance, final String name) {
        super(instance, name, true);
        this.addSubCommand(new SCmdAdd(this.plugin, this));
        this.addSubCommand(new SCmdCheck(this.plugin, this));
        this.addSubCommand(new SCmdRemove(this.plugin, this));
    }

    @Override
    protected void addSubCommand(final SubCommand sc) {
        super.addSubCommand(sc);
    }
}
