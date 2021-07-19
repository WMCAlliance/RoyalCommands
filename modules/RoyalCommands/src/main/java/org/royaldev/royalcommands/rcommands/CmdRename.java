/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdRename extends TabCommand {

    public CmdRename(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        String newName = RUtils.colorize(RoyalCommands.getFinalArg(args, 0));
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't rename air!");
            return true;
        }
        switch (hand.getType()) {
            case BREWING_STAND:
            case DISPENSER:
            case DROPPER:
            case FURNACE:
            case HOPPER:
            case HOPPER_MINECART:
            case CHEST_MINECART:
			case BAT_SPAWN_EGG:
			case BLAZE_SPAWN_EGG:
			case CAVE_SPIDER_SPAWN_EGG:
			case CHICKEN_SPAWN_EGG:
			case COD_SPAWN_EGG:
			case COW_SPAWN_EGG:
			case CREEPER_SPAWN_EGG:
			case DOLPHIN_SPAWN_EGG:
			case DONKEY_SPAWN_EGG:
			case DROWNED_SPAWN_EGG:
			case ELDER_GUARDIAN_SPAWN_EGG:
			case ENDERMAN_SPAWN_EGG:
			case ENDERMITE_SPAWN_EGG:
			case EVOKER_SPAWN_EGG:
			case GHAST_SPAWN_EGG:
			case GUARDIAN_SPAWN_EGG:
			case HORSE_SPAWN_EGG:
			case HUSK_SPAWN_EGG:
			case LLAMA_SPAWN_EGG:
			case MAGMA_CUBE_SPAWN_EGG:
			case MOOSHROOM_SPAWN_EGG:
			case MULE_SPAWN_EGG:
			case OCELOT_SPAWN_EGG:
			case PARROT_SPAWN_EGG:
			case PHANTOM_SPAWN_EGG:
			case PIG_SPAWN_EGG:
			case POLAR_BEAR_SPAWN_EGG:
			case PUFFERFISH_SPAWN_EGG:
			case RABBIT_SPAWN_EGG:
			case SALMON_SPAWN_EGG:
			case SHEEP_SPAWN_EGG:
			case SHULKER_SPAWN_EGG:
			case SILVERFISH_SPAWN_EGG:
			case SKELETON_HORSE_SPAWN_EGG:
			case SKELETON_SPAWN_EGG:
			case SLIME_SPAWN_EGG:
			case SPIDER_SPAWN_EGG:
			case SQUID_SPAWN_EGG:
			case STRAY_SPAWN_EGG:
			case TROPICAL_FISH_SPAWN_EGG:
			case TURTLE_SPAWN_EGG:
			case VEX_SPAWN_EGG:
			case VILLAGER_SPAWN_EGG:
			case VINDICATOR_SPAWN_EGG:
			case WITCH_SPAWN_EGG:
			case WITHER_SKELETON_SPAWN_EGG:
			case WOLF_SPAWN_EGG:
			case ZOMBIE_HORSE_SPAWN_EGG:
			case ZOMBIFIED_PIGLIN_SPAWN_EGG:
			case ZOMBIE_SPAWN_EGG:
			case ZOMBIE_VILLAGER_SPAWN_EGG:
            case CHEST:
			case ENDER_CHEST:
			case TRAPPED_CHEST:
                if (newName.length() > 32) newName = newName.substring(0, 32);
                cs.sendMessage(MessageColor.POSITIVE + "The new name has been shortened to " + MessageColor.NEUTRAL + newName + MessageColor.POSITIVE + " to prevent crashes.");
        }
        ItemStack is = RUtils.renameItem(hand, newName);
        p.getInventory().setItemInMainHand(is);
        cs.sendMessage(MessageColor.POSITIVE + "Renamed your " + MessageColor.NEUTRAL + RUtils.getItemName(is) + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + newName + MessageColor.POSITIVE + ".");
        return true;
    }
}
