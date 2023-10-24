/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.sign.Side;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdSignEdit extends TabCommand {

    public CmdSignEdit(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }
	
    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        Block b = p.getTargetBlock((Set<Material>) null, 100); // RUtils has signs as transparent;
        if (b == null || !(b.getState() instanceof Sign s)) {
            cs.sendMessage(MessageColor.NEGATIVE + "The block in sight is not a sign!");
            return true;
        }
		BlockData blockData = s.getBlockData();
		BlockFace signFace;
		if (blockData instanceof Directional directional) {
            signFace = directional.getFacing();
        } else if (blockData instanceof Rotatable rotatable) {
            signFace = rotatable.getRotation();
        } else {
            signFace = BlockFace.NORTH;
        }
		Vector signDirection = new Vector(signFace.getModX(), signFace.getModY(), signFace.getModZ());
        Vector vector = p.getEyeLocation().toVector().subtract(s.getLocation().add(0.5, 0.5, 0.5).toVector());
        p.openSign(s, vector.dot(signDirection) > 0 ? Side.FRONT : Side.BACK);
        return true;
    }
}
