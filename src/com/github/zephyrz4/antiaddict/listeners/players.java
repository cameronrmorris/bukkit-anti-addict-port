package com.github.zephyrz4.antiaddict.listeners;

import java.util.HashMap;
import org.bukkit.ChatColor;
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

	/// Map that stores the time a player joined if they are an addict
	public static HashMap<String, Long> jointimesave = new HashMap<String, Long>();
	/// Map that stores the time a player has left on the server
	public static HashMap<String, Long> resttimelist = new HashMap<String, Long>();
	/// Map that stores the time a player has left on the server derp?
	public static HashMap<String, Long> playtimesave = new HashMap<String, Long>();
	// FIXME Somehow merge these or get rid of them it's stupid
	/// Stores how long a player has been on the server
	long playtime;
	/// Stores how long a player has been on the server
	long playtimeold;
	/// Instance of the plugin
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
	 * @param event
	 *            passed by bukkit to retrieve the player
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName().toLowerCase();

		if (antiaddict.status) {
			if ((antiaddict.addicts.contains(playername))
					|| (antiaddict.limitall)) {
				jointimesave.put(playername,
						Long.valueOf(System.currentTimeMillis()));

				plugin.getLogger().info(
						"The player " + playername + " just logged in.");
				plugin.getLogger().info(
						"He was marked as addicted, so his playtime");
				plugin.getLogger().info(
						"is restricted to " + (antiaddict.timelimit / 60000L)
								+ " minutes.");
				player.sendMessage(antiaddict.joinmessagePart1 + " "
						+ ChatColor.RED + antiaddict.timelimit
						+ ChatColor.WHITE + " " + antiaddict.joinmessagePart2);
			}
		}
	}

	/**
	 * Saves the value of played time for this player upon leaving the server
	 * 
	 * @param event
	 *            passed by bukkit to retrieve the player
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
	 * @param event
	 *            passed by bukkit to retrieve the player
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName().toLowerCase();
		Long jointime;
		if (antiaddict.status) {
			if (((antiaddict.limitall) || (antiaddict.addicts
					.contains(playername)))
					&& (!player.hasPermission("antiaddict.ignorelimits"))) {

				try {
					jointime = ((Long) jointimesave.get(playername))
							.longValue();
					this.playtimeold = ((Long) playtimesave.get(playername))
							.longValue();
				} catch (NullPointerException nfe) {
					jointimesave.put(playername,
							Long.valueOf(System.currentTimeMillis()));
					jointime = System.currentTimeMillis();
					playtimesave.put(playername, 0L);
					this.playtimeold = 0L;
				}

				this.playtime = (this.playtimeold + (System.currentTimeMillis() - jointime));
				long resttime = antiaddict.timelimit - this.playtime;

				resttimelist.put(playername, Long.valueOf(resttime));
				if (resttime <= 0L) {
					player.kickPlayer(antiaddict.limitkickmessage);

					plugin.getLogger()
							.info(playername
									+ " reached his daily limit and was kicked into RL again.");
				}
			}
		}
	}
}