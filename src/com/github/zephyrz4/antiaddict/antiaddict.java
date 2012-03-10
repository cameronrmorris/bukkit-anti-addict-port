package com.github.zephyrz4.antiaddict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.zephyrz4.antiaddict.listeners.players;

/**
 * Main class for the antiaddict plugin which manages players which have been
 * flagged for a limited time on the server.
 */
public class antiaddict extends JavaPlugin {
	/// Used  to load the plugin in to the server.
	PluginManager pm;
	/// Flag for if the plugin is enabled
	public static boolean status = true;
	/// Flag for if to limit all players
	public static boolean limitall = false;
	/// Time limit for all players
	public static long timelimit;
	/// Message displayed to users when kicked
	public static String limitkickmessage ;
	// FIXME MAKE THIS ONE VARIABLE PLEASE
	/// Message to player when they join
	public static String joinmessagePart1;
	/// Message to player when they join
	public static String joinmessagePart2;
	/// When the player joins the server. Used to track time
	long jointime ;
	/// How long the player has been the server
	long playtime ;
	/// Stored value of how long the player has played
	long playtimeold;
	/// List of players who are limited
	public static List<Object> addicts;
	/// Configuration file stored in config.yml
	File configFile;
	/// Bukkit implementation of accessing the config.yml
	FileConfiguration config ;
	/// Used to log to the console
	private Logger log = this.getLogger();

	/**
	 * Runs when the server is shutting down or pre-reload
	 */
	public void onDisable() {
		getLog().info("AntiAddict has been disabled!");
	}

	/**
	 * Runs when server is starting up or post-reload
	 */
	public void onEnable() {

		// Logs the startup the plugin
		getLog().info("[AntiAddict] Enabling AntiAddict...");

		// Checks if the config exists
		this.configFile = new File(getDataFolder(), "config.yml");
		if (!this.configFile.exists()) {
			try {
				firstRun();
			} catch (Exception e) {
				getLog().info(
						"[AntiAddict][Important] A serious problem occured!");
				getLog().info("[AntiAddict][Important] Please report this!");
				e.printStackTrace();
			}
		}

		// Load the config
		this.config = new YamlConfiguration();
		loadYamls();

		getLog().info("AntiAddict has been enabled!");

		// Load the event listener and pass it the server to manage them
		Listener player = new players(this);

		this.pm = getServer().getPluginManager();

		pm.registerEvents(player, this);
	}

	/**
	 * Creates the the default config file. Throws an exception if there is a
	 * problem creating it
	 * 
	 * @throws Exception
	 *             IO error from creation of config
	 */
	private void firstRun() throws Exception {
		getLog().info("[AntiAddict] AntiAddict is run for the first time...");
		getLog().info("[AntiAddict] No Config found!");
		getLog().info("[AntiAddict] Creating default Config!");

		// Creates the config directory
		this.configFile.getParentFile().mkdirs();

		// Copys the default config file from the JAR
		copy(getResource("config.yml"), this.configFile);

		getLog().info("[AntiAddict] Default Config was created successfully!");
	}

	/**
	 * Utility function to copy the config file from the JAR
	 * 
	 * @param in
	 *            stream of the default config file to be copied
	 * @param file
	 *            output file of the copy of the file
	 */
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the config file from the config directory
	 */
	public void loadYamls() {
		try {
			this.config.load(this.configFile);

			joinmessagePart1 = this.config.getString("JoinMessage.Part1");
			joinmessagePart2 = this.config.getString("JoinMessage.Part2");

			timelimit = this.config.getInt("AntiAddict.Timelimit");
			limitkickmessage = this.config
					.getString("AntiAddict.LimitKickMessage");
			addicts = this.config.getList("Addicts");

			limitall = this.config.getBoolean("AntiAddict.LimitAll");

			timelimit = timelimit * 60000L;

			getLog().info("[AntiAddict] Config has been loaded successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			getLog().info("[AntiAddict] No Config found!");
		}
	}

	/**
	 * Saves the config file to the disk
	 */
	public void saveYamls() {
		try {
			this.config.save(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Bukkit method for handling the commands of the plugin on = enables the
	 * plugin off = disables the plugin left = tells the player the amount of
	 * time they have left
	 */
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("antiaddict")) {
			return false;
		}
		if (args.length <= 0) {
			showUsage(sender);
			return false;
		}
		if (args[0].equalsIgnoreCase("on"))
			enableAntiAddict(sender);
		else if (args[0].equalsIgnoreCase("off"))
			disableAntiAddict(sender);
		else if (args[0].equalsIgnoreCase("left"))
			showTimeLeft(sender);
		else {
			showUsage(sender);
		}
		return false;
	}

	/**
	 * Prints to the user the commands of the plugin
	 * 
	 * @param sender player who executed the command
	 */
	private void showUsage(CommandSender sender) {
		sender.sendMessage("Use:");
		sender.sendMessage(ChatColor.RED + "/antiaddict on" + ChatColor.WHITE
				+ " - Enables the plugin.");
		sender.sendMessage(ChatColor.RED + "/antiaddict off" + ChatColor.WHITE
				+ " - Disables the plugin");
		sender.sendMessage(ChatColor.RED + "/antiaddict left" + ChatColor.WHITE
				+ " - Print how much time is left");
	}

	/**
	 * Calculates and prints to the user the amount of time remaining
	 * 
	 * @param sender player who executed the command
	 */
	private void showTimeLeft(CommandSender sender) {
		if ((sender instanceof Player)) {
			Player player = ((Player) sender).getPlayer();
			String playername = player.getName().toLowerCase();

			this.jointime = ((Long) players.jointimesave.get(playername))
					.longValue();
			try {
				this.playtimeold = ((Long) players.playtimesave.get(playername))
						.longValue();
			} catch (NullPointerException nfe) {
				players.playtimesave.put(playername, Long.valueOf(0L));
				this.playtimeold = ((Long) players.playtimesave.get(playername))
						.longValue();
			}

			this.playtime = (this.playtimeold + (System.currentTimeMillis() - this.jointime));
			long resttime = (timelimit - this.playtime) / 60000L;
			player.sendMessage("You have " + resttime + " minutes left today!");
		} else {
			getLog().info("This command can only be used ingame!");
		}
	}

	/**
	 * Enables the plugin if the player has the proper permissions
	 * 
	 * @param senderplayer who executed the command
	 */
	private void enableAntiAddict(CommandSender sender) {
		if ((sender.hasPermission("antiaddict.admin")) || (sender.isOp())) {
			status = true;
			sender.sendMessage(ChatColor.GREEN + "AntiAddict has been enabled!");
		} else {
			sender.sendMessage("You are no Admin...");
		}
	}

	/**
	 * Disable the plugin if the player has the proper permissions
	 * 
	 * @param sender player who executed the command
	 */
	private void disableAntiAddict(CommandSender sender) {
		if ((sender.hasPermission("antiaddict.admin")) || (sender.isOp())) {
			status = false;
			sender.sendMessage(ChatColor.RED + "Antiaddict has been disabled!");
		} else {
			sender.sendMessage("You are no Admin...");
		}
	}

	/**
	 * Gets the instance of the logger
	 * 
	 * @return the logger object
	 */
	Logger getLog() {
		return log;
	}

	/**
	 * Sets the logger object
	 * 
	 * @param log
	 */
	void setLog(Logger log) {
		this.log = log;
	}
}