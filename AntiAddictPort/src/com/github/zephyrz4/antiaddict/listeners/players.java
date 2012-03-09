package com.github.zephyrz4.antiaddict.listeners;

import java.util.HashMap;
import java.util.Timer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.github.zephyrz4.antiaddict.antiaddict;

/**
 * Manages the ingame events that trigger this plugin
 * 
 * @author Cam
 * 
 */
public class players implements Listener {
	public static GameMode mode;
	public static ChatColor red = ChatColor.RED;
	public static ChatColor green = ChatColor.GREEN;
	public static ChatColor white = ChatColor.WHITE;

	public static HashMap<String, Long> jointimesave = new HashMap<String, Long>();

	public static HashMap<String, Long> resttimelist = new HashMap<String, Long>();

	public static HashMap<String, Long> playtimesave = new HashMap<String, Long>();
	long jointime;
	long playtime;
	long currenttime;
	long playtimeold;
	Timer timer = new Timer();
	antiaddict plugin;

	/**
	 * Constructor that sets the plugin for this listener
	 * 
	 * @param plugin
	 *            instance of the plugin
	 */
	public players(antiaddict plugin) {
		this.plugin = plugin;
	}

	/**
	 * Sets the player to the addict management system if they are set in the
	 * config file as an addict
	 * 
	 * @param event passed by bukkit to retrieve the player
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName().toLowerCase();

		if (antiaddict.status) {
			if ((antiaddict.addicts.contains(playername))
					|| (antiaddict.limitall)) {
				this.jointime = System.currentTimeMillis();
				jointimesave.put(playername, Long.valueOf(this.jointime));

				plugin.getLogger().info(
						"[AntiAddict] The player " + playername
								+ " just logged in.");
				plugin.getLogger()
						.info("[AntiAddict] He was marked as addicted, so his playtime");
				plugin.getLogger().info(
						"[AntiAddict] is restricted to " + antiaddict.timelimit
								+ " minutes.");
				player.sendMessage(antiaddict.joinmessagePart1 + " " + red
						+ antiaddict.timelimit + white + " "
						+ antiaddict.joinmessagePart2);
			}
		}
	}

	/**
	 * Saves the value of played time for this player upon leaving the server
	 * 
	 * @param event passed by bukkit to retrieve the player
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName().toLowerCase();

		if (antiaddict.status) {
			if ((antiaddict.addicts.contains(playername))
					|| (antiaddict.limitall)) {
				playtimesave.put(playername, Long.valueOf(this.playtime));
			}
		}
	}

	/**
	 * Checks if the player should be removed from the server
	 * 
	 * @param event passed by bukkit to retrieve the player
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName().toLowerCase();

		if (antiaddict.status) {
			if (((antiaddict.limitall) || (antiaddict.addicts
					.contains(playername)))
					&& (!player.hasPermission("antiaddict.ignorelimits"))) {
				this.currenttime = System.currentTimeMillis();
				this.jointime = ((Long) jointimesave.get(playername))
						.longValue();
				try {
					this.playtimeold = ((Long) playtimesave.get(playername))
							.longValue();
				} catch (NullPointerException nfe) {
					playtimesave.put(playername, Long.valueOf(0L));
					this.playtimeold = ((Long) playtimesave.get(playername))
							.longValue();
				}

				this.playtime = (this.playtimeold + (this.currenttime - this.jointime));
				long resttime = antiaddict.timelimitmil - this.playtime;

				resttimelist.put(playername, Long.valueOf(resttime));

				if (resttime <= 0L) {
					player.kickPlayer(antiaddict.limitkickmessage);

					plugin.getLogger()
							.info("[AntiAddict] "
									+ playername
									+ " reached his daily limit and was kicked into RL again.");
				}
			}
		}
	}
}