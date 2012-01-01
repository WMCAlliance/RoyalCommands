package tk.royalcraf.rcommands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import tk.royalcraf.royalcommands.RoyalCommands;

public class Unban implements CommandExecutor {

	RoyalCommands plugin;

	public Unban(RoyalCommands plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("unban")) {
			if (!plugin.isAuthorized(cs, "rcmds.unban")) {
				cs.sendMessage(ChatColor.RED
						+ "You don't have permission for that!");
				plugin.log.warning("[RoyalCommands] " + cs.getName()
						+ " was denied access to the command!");
				return true;
			}
			if (args.length < 1) {
				return false;
			}
			OfflinePlayer t = plugin.getServer().getOfflinePlayer(
					args[0].trim());
			if (!t.isBanned()) {
				cs.sendMessage(ChatColor.RED + "That player isn't banned!");
				return true;
			}
			t.setBanned(false);
			cs.sendMessage(ChatColor.BLUE + "You have unbanned "
					+ ChatColor.GRAY + t.getName() + ChatColor.BLUE + ".");
			plugin.getServer().broadcast(
					ChatColor.BLUE + "The player " + ChatColor.GRAY
							+ cs.getName() + ChatColor.BLUE + " has unbanned "
							+ ChatColor.GRAY + t.getName() + ChatColor.BLUE
							+ ".", "rcmds.see.unban");
			return true;
		}
		return false;
	}

}