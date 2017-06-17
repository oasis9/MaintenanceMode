package co.aeva.maintenancemode;

import static net.md_5.bungee.api.ChatColor.AQUA;
import static net.md_5.bungee.api.ChatColor.GRAY;
import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;
import static net.md_5.bungee.api.ChatColor.WHITE;
import static net.md_5.bungee.api.ChatColor.YELLOW;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MaintenanceMode extends JavaPlugin implements Listener {
	
	static MaintenanceMode mm;
	boolean enabled = true;
	String motdActive = ChatColor.translateAlternateColorCodes('&', "&cDown for maintenance!\n&6We'll be back shortly.");
	String motdInactive = ChatColor.translateAlternateColorCodes('&', "&7Just another network..");
	String kick = ChatColor.translateAlternateColorCodes('&',
			"&6We're down for maintenance. Check back later!");
	int allow = 0;

	public static MaintenanceMode mm() {
		return mm;
	}
	
	@Override
	public void onEnable() {
		mm = this;
		Bukkit.getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		enabled = getConfig().getBoolean("enabled");
		motdActive = ChatColor.translateAlternateColorCodes('&', getConfig().getString("motd.active"));
		motdInactive = ChatColor.translateAlternateColorCodes('&', getConfig().getString("motd.inactive"));
		kick = ChatColor.translateAlternateColorCodes('&', getConfig().getString("kick"));
		new BukkitRunnable() {
			@Override
			public void run() {
				if (enabled)
					Bukkit.broadcastMessage(YELLOW + "Maintenance mode " + RED + "ACTIVE");
			}
		}.runTaskTimerAsynchronously(this, 0, 20 * 180);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender.hasPermission("maintenance.status") || sender.hasPermission("maintenance.toggle")
				|| sender.hasPermission("maintenance.motd"))) {
			sender.sendMessage(RED + "You aren't allowed to do this!");
			return true;
		}
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("toggle")) {
				if (sender.hasPermission("maintenance.activate")) {
					if (!setMaintenance(!enabled))
						sender.sendMessage(RED + "Maintenance mode was not enabled due to another plugin cancelling it.");
				} else
					sender.sendMessage(RED + "You aren't allowed to do this!");
			} else if (args[0].equalsIgnoreCase("allow")) {
				if (sender.hasPermission("maintenance.activate")) {
					if (args.length > 1)
						try {
							allow = Integer.valueOf(args[1]);
							sender.sendMessage(GREEN + "Allowing " + AQUA + allow + GREEN + " unwhitelisted players.");
						} catch (Exception ex) {
							sender.sendMessage(RED + "/mm allow [num]");
						}
					else
						sender.sendMessage(RED + "/mm allow [num]");
				} else
					sender.sendMessage(RED + "You aren't allowed to do this!");
			} else if (args[0].equalsIgnoreCase("motd")) {
				if (sender.hasPermission("maintenance.motd"))
					if (args.length > 2) {
						if (args[1].equalsIgnoreCase("active")) {
							motdActive = ChatColor
									.translateAlternateColorCodes('&', StringUtils.join(args, " ", 2, args.length))
									.replace("\\n", "\n");
							getConfig().set("motd.active", motdActive);
						} else if (args[1].equalsIgnoreCase("inactive")) {
							motdInactive = ChatColor
									.translateAlternateColorCodes('&', StringUtils.join(args, " ", 2, args.length))
									.replace("\\n", "\n");
							getConfig().set("motd.inactive", motdInactive);
						} else {
							sender.sendMessage(YELLOW + "/" + label + " motd [active|inactive] [motd" + AQUA
									+ "\\n" + YELLOW + "newline]");
							return true;
						}
						saveConfig();
						sender.sendMessage(YELLOW + "Maintenance mode " + RED + "ACTIVE" + YELLOW + " MOTD:\n" + GRAY
								+ motdActive + "\n\n" + YELLOW + "Maintenance mode " + GREEN + "INACTIVE" + YELLOW
								+ " MOTD:\n" + GRAY + motdInactive);
					} else
						sender.sendMessage(YELLOW + "/" + label + " motd [active|inactive] [msg" + AQUA + "\\n"
								+ YELLOW + "newline]");
				else
					sender.sendMessage(RED + "You aren't allowed to do this!");
			} else if (args[0].equalsIgnoreCase("kick")) {
				if (sender.hasPermission("maintenance.kick"))
					if (args.length > 1) {
						kick = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, " ", 1, args.length))
								.replace("\\n", "\n");
						getConfig().set("kick", kick);
						saveConfig();
						sender.sendMessage(YELLOW + "Maintenance mode " + RED + "ACTIVE" + YELLOW + " kick message:\n"
								+ WHITE + kick);
					} else
						sender.sendMessage(
								YELLOW + "/" + label + " kick [msg" + AQUA + "\\n" + YELLOW + "newline]");
				else
					sender.sendMessage(RED + "You aren't allowed to do this!");
			} else
				sender.sendMessage(YELLOW + "/" + label + " [toggle|motd [active|inactive] [msg" + AQUA + "\\n"
						+ YELLOW + "newline]|kick [msg" + AQUA + "\\n" + YELLOW + "newline]]");
		} else
			sender.sendMessage(YELLOW + "/" + label + " [toggle|motd [active|inactive] [msg" + AQUA + "\\n"
					+ YELLOW + "newline]]");
		return true;
	}
	
	public boolean getMaintenance() {
		return enabled;
	}
	
	public boolean setMaintenance(boolean enabled) {
		this.enabled = enabled;
		MaintenanceModeEvent e = new MaintenanceModeEvent();
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled())
			return false;
		if (enabled)
			for (Player pl : Bukkit.getOnlinePlayers())
				if (!pl.isWhitelisted())
					pl.kickPlayer(kick);
		Bukkit.broadcastMessage(YELLOW + "Maintenance mode " + (enabled ? RED + "ACTIVATED" : GREEN + "DEACTIVATED"));
		return true;
	}
	
	@EventHandler
	public void playerLogin(PlayerLoginEvent e) {
		if (enabled && e.getPlayer().hasPermission("maintenance.admin"))
			if (allow > 0)
				allow--;
			else {
				e.setResult(Result.KICK_OTHER);
				e.setKickMessage(kick);
			}
	}
}