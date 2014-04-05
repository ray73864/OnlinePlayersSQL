package net.rayherring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

public class MyOnlPlayerListener implements Listener {
  Logger log = Logger.getLogger("Minecraft");
  OnlinePlayersSQL plugin;
  String sqlTable;
  ResultSet result;
  
  public MyOnlPlayerListener(OnlinePlayersSQL plugin)
  {
    this.plugin = plugin;
    this.sqlTable = this.plugin.opConfig.getMySQLTable();
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }
  
  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerDeath(PlayerDeathEvent event)
  {
    ResultSet result = null;
    int currentDeathCount = 0;
    Player victim = event.getEntity().getPlayer();
    Player killer = event.getEntity().getKiller();
    Connection conn = null;
    
    OnlinePlayersSQLQuery myQuery = null;
    
    if ((this.plugin.opConfig.trackOnlyAllowedPlayers()) && (!killer.hasPermission("onlineplayerssql.allowed"))) {
      return;
    }
    
    myQuery = new OnlinePlayersSQLQuery("SELECT player_deaths FROM " + sqlTable + " WHERE player = ?", victim.getName());
    PreparedStatement playerDeaths = null;
    try {
    	conn = plugin.opSql.SQLConnect();
		playerDeaths = conn.prepareStatement(myQuery.getQuery());
		result = this.plugin.opSql.runSearchQueryNew(conn, myQuery, playerDeaths);
		if ( result.next() ) {
    		currentDeathCount = result.getInt(1) + 1;
    	    
    	    myQuery = new OnlinePlayersSQLQuery("UPDATE " + sqlTable + " SET player_deaths=? WHERE player=?", currentDeathCount, victim.getName());
    	    this.plugin.opSql.runUpdateQueryNew(myQuery);	
    	}
	} catch (SQLException e1) {
		e1.printStackTrace();
	} finally {
		try { conn.close(); } catch ( SQLException e ) { }
	}
    
    if (killer != null)
    {
    	myQuery = new OnlinePlayersSQLQuery("SELECT player_kills FROM " + sqlTable + " WHERE player = ?", killer.getName());
    	PreparedStatement playerKills = null;
    	try {
    		conn = plugin.opSql.SQLConnect();
    		playerKills = conn.prepareStatement(myQuery.getQuery());
    		result = this.plugin.opSql.runSearchQueryNew(conn, myQuery, playerKills);
    		
    		if ( result.next() ) {
        		currentDeathCount = result.getInt(1) + 1;

        		myQuery = new OnlinePlayersSQLQuery("UPDATE " + sqlTable + " SET player_kills=? WHERE player=?", currentDeathCount, killer.getName());
        		this.plugin.opSql.runUpdateQueryNew(myQuery);
        	}
    	} catch (SQLException e1) {
    		e1.printStackTrace();
    	} finally {
    		try { conn.close(); } catch ( SQLException e ) { }
    	}
    }
  }
  
  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerJoin(PlayerJoinEvent event)
  {
    int firstLogon = (int)event.getPlayer().getFirstPlayed();
    int loginTime = (int)(System.currentTimeMillis() / 1000L);
    Player player = event.getPlayer();
    
    if ((this.plugin.opConfig.trackOnlyAllowedPlayers()) && (!event.getPlayer().hasPermission("onlineplayerssql.allowed"))) {
      return;
    }
    if (this.plugin.opConfig.isShowDebug()) {
      this.log.info("Player World Join: " + event.getPlayer().getName() + " " + event.getPlayer().getWorld().getName());
    }
    
    this.plugin.updatePlayerRecord(event.getPlayer());
  }
  
  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerWorldChange(PlayerChangedWorldEvent event)
  {
	  String previousWorld = event.getFrom().getName();
	  String currentWorld = event.getPlayer().getWorld().getName();
	  String player = event.getPlayer().getName();
	  String primaryGroup = "";
	  
	  try {
		  primaryGroup = this.plugin.permission.getPrimaryGroup(event.getPlayer().getWorld().getName(), event.getPlayer().getName());
	  } catch (UnsupportedOperationException e) {
		  primaryGroup = "";
	  }
	  if ((this.plugin.opConfig.trackOnlyAllowedPlayers()) && (!event.getPlayer().hasPermission("onlineplayerssql.allowed"))) {
		  return;
	  }
	  if (this.plugin.opConfig.isShowDebug()) {
		  this.log.info("Player World " + event.getPlayer().getName() + " " + event.getPlayer().getWorld().getName());
	  }
    
	  OnlinePlayersSQLQuery myQuery = new OnlinePlayersSQLQuery(
    		"UPDATE " + sqlTable + " SET previous_world=?, current_world=?, permission_group=? WHERE player=?",
    			previousWorld, currentWorld, primaryGroup, player
		);

	  this.plugin.opSql.runUpdateQueryNew(myQuery);
  }
  
  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerDisconnect(PlayerQuitEvent event)
  {
    int logoutTime = (int)(System.currentTimeMillis() / 1000L);
    
    String player = event.getPlayer().getName();
    
    if ((this.plugin.opConfig.trackOnlyAllowedPlayers()) && (!event.getPlayer().hasPermission("onlineplayerssql.allowed"))) {
      return;
    }
    if (this.plugin.opConfig.isShowDebug()) {
      this.log.info("Player Disconnected " + event.getPlayer().getName() + ".");
    }
    
    OnlinePlayersSQLQuery myQuery = new OnlinePlayersSQLQuery("UPDATE " + sqlTable + " SET online = ?, last_logout = ? WHERE player = ?", false, logoutTime, player);
    this.plugin.opSql.runUpdateQueryNew(myQuery);
  }
}
