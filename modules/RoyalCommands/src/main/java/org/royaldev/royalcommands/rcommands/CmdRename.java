/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.EnumSet;
import java.util.Set;
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
        if (hand == null || RUtils.isBlockAir(hand.getType())) {
            cs.sendMessage(MessageColor.NEGATIVE + "You can't rename air!");
            return true;
        }
        Set<Material> containerTypes = EnumSet.of(
            Material.BARREL,
            Material.BREWING_STAND,
            Material.CHEST_MINECART,
            Material.CHEST,
            Material.DISPENSER,
            Material.DROPPER,
			Material.ENDER_CHEST,
            Material.FURNACE,
            Material.HOPPER,
            Material.HOPPER_MINECART,
			Material.SHULKER_BOX,
			Material.TRAPPED_CHEST
        );
        Set<Material> spawneggTypes = EnumSet.of(
			Material.BLAZE_SPAWN_EGG,
			Material.CAVE_SPIDER_SPAWN_EGG,
			Material.CHICKEN_SPAWN_EGG,
			Material.COD_SPAWN_EGG,
			Material.COW_SPAWN_EGG,
			Material.CREEPER_SPAWN_EGG,
			Material.DOLPHIN_SPAWN_EGG,
			Material.DONKEY_SPAWN_EGG,
			Material.DROWNED_SPAWN_EGG,
			Material.ELDER_GUARDIAN_SPAWN_EGG,
			Material.ENDERMAN_SPAWN_EGG,
			Material.ENDERMITE_SPAWN_EGG,
			Material.EVOKER_SPAWN_EGG,
			Material.GHAST_SPAWN_EGG,
			Material.GUARDIAN_SPAWN_EGG,
			Material.HORSE_SPAWN_EGG,
			Material.HUSK_SPAWN_EGG,
			Material.LLAMA_SPAWN_EGG,
			Material.MAGMA_CUBE_SPAWN_EGG,
			Material.MOOSHROOM_SPAWN_EGG,
			Material.MULE_SPAWN_EGG,
			Material.OCELOT_SPAWN_EGG,
			Material.PARROT_SPAWN_EGG,
			Material.PHANTOM_SPAWN_EGG,
			Material.PIG_SPAWN_EGG,
			Material.POLAR_BEAR_SPAWN_EGG,
			Material.PUFFERFISH_SPAWN_EGG,
			Material.RABBIT_SPAWN_EGG,
			Material.SALMON_SPAWN_EGG,
			Material.SHEEP_SPAWN_EGG,
			Material.SHULKER_SPAWN_EGG,
			Material.SILVERFISH_SPAWN_EGG,
			Material.SKELETON_HORSE_SPAWN_EGG,
			Material.SKELETON_SPAWN_EGG,
			Material.SLIME_SPAWN_EGG,
			Material.SPIDER_SPAWN_EGG,
			Material.SQUID_SPAWN_EGG,
			Material.STRAY_SPAWN_EGG,
			Material.TROPICAL_FISH_SPAWN_EGG,
			Material.TURTLE_SPAWN_EGG,
			Material.VEX_SPAWN_EGG,
			Material.VILLAGER_SPAWN_EGG,
			Material.VINDICATOR_SPAWN_EGG,
			Material.WITCH_SPAWN_EGG,
			Material.WITHER_SKELETON_SPAWN_EGG,
			Material.WOLF_SPAWN_EGG,
			Material.ZOMBIE_HORSE_SPAWN_EGG,
			Material.ZOMBIFIED_PIGLIN_SPAWN_EGG,
			Material.ZOMBIE_SPAWN_EGG,
			Material.ZOMBIE_VILLAGER_SPAWN_EGG
        );
        int maxName = 32;
        if (newName.length() > maxName && (containerTypes.contains(hand.getType()) || spawneggTypes.contains(hand.getType()))) {
            newName = newName.substring(0, maxName);
            cs.sendMessage(MessageColor.POSITIVE + "The new name has been shortened to " + MessageColor.NEUTRAL + newName + MessageColor.POSITIVE + " to prevent crashes.");
        }
        ItemStack is = RUtils.renameItem(hand, newName);
        p.getInventory().setItemInMainHand(is);
        cs.sendMessage(MessageColor.POSITIVE + "Renamed your " + MessageColor.NEUTRAL + RUtils.getItemName(is) + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + newName + MessageColor.POSITIVE + ".");
        return true;
    }
}
