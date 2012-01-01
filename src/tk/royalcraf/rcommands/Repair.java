package tk.royalcraf.rcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tk.royalcraf.royalcommands.RoyalCommands;

public class Repair implements CommandExecutor {

	RoyalCommands plugin;

	public Repair(RoyalCommands plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("repair")) {
			if (!plugin.isAuthorized(cs, "rcmds.repair")) {
				cs.sendMessage(ChatColor.RED
						+ "You don't have permission for that!");
				plugin.log.warning("[RoyalCommands] " + cs.getName()
						+ " was denied access to the command!");
				return true;
			}
			if (!(cs instanceof Player)) {
				cs.sendMessage(ChatColor.RED
						+ "This command is only available to players!");
				return true;
			}
			/*
			 * if (args.length < 1) { return false; }
			 */
			Player p = (Player) cs;
			ItemStack hand = p.getItemInHand();
			if (hand.getTypeId() == 0) {
				cs.sendMessage(ChatColor.RED + "You can't repair air!");
				return true;
			}
			hand.setDurability((short) 0);
			cs.sendMessage(ChatColor.BLUE + "Fixed your " + ChatColor.GRAY
					+ hand.getType().toString().toLowerCase().replace("_", " ")
					+ ChatColor.BLUE + ".");
			return true;
		}
		return false;
	}

}