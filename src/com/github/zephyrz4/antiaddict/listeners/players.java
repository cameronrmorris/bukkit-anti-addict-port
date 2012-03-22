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
  private static HashMap<String, Long> jointimeMap = new HashMap<String, Long>();
  /// Map that stores the time a player has left on the server 
  private static HashMap<String, Long> playtimeMap = new HashMap<String, Long>();
  /// Instance of the plugin
  antiaddict plugin;
 
  
  /**
   * Constructor that sets the plugin for this listener
   * 
   * @param plugin instance of the plugin
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
      if ((antiaddict.addicts.contains(playername)) || (antiaddict.limitall)) {
        getJoinTimeMap().put(playername, System.currentTimeMillis());

        // Reset?
        // if (the date has increased by one then ) {
        //  getPlaytimeMap().put(playername, 0L) ;
        //}
        
        plugin.getLogger().info(
            playername + " is restricted to " + (antiaddict.timelimit / 60000L)
                + " minutes.");
        player.sendMessage(antiaddict.joinmessagePart1 + " " + ChatColor.RED
            + (antiaddict.timelimit / 60000L) + ChatColor.WHITE + " "
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
    long newtime;
    long oldtime ;
    long jointime ;
    if (antiaddict.status) {
      if ((antiaddict.addicts.contains(playername)) || (antiaddict.limitall)) {
        jointime = getJoinTimeMap().get(playername) ;
        oldtime = getPlaytimeMap().get(playername) ;
        newtime = oldtime + (System.currentTimeMillis() - jointime);
        
        getPlaytimeMap().put(playername, newtime);

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
    
    long jointime = 0L;
    long resttime = 0L;
    long playtime = 0L;
    long oldtime = 0L ;

    if (antiaddict.status) {
      if (((antiaddict.limitall) || (antiaddict.addicts.contains(playername)))
          && (!player.hasPermission("antiaddict.ignorelimits"))) {

        try {
          oldtime = getPlaytimeMap().get(playername);
          jointime = (getJoinTimeMap().get(playername));
        } catch (NullPointerException nfe) {
          
          //If the times are missing (caused by reload) then reset some things:
          getPlaytimeMap().put(playername, 0L) ;
          oldtime = 0L ;
          getJoinTimeMap().put(playername, System.currentTimeMillis());
          jointime = System.currentTimeMillis();
        }

        playtime = oldtime + (System.currentTimeMillis() - jointime);

        resttime = antiaddict.timelimit - playtime;
      
        if (resttime <= 0L) {
          player.kickPlayer(antiaddict.limitkickmessage);

          plugin.getLogger().info(playername + " reached limit.");
        }
      }
    }
  }

  public static HashMap<String, Long> getJoinTimeMap() {
    return jointimeMap;
  }

  public static void setJoinTimeMap(HashMap<String, Long> jointimeMap) {
    players.jointimeMap = jointimeMap;
  }

  public static HashMap<String, Long> getPlaytimeMap() {
    return playtimeMap;
  }

  public static void setPlaytimeMap(HashMap<String, Long> playtimeMap) {
    players.playtimeMap = playtimeMap;
  }




}