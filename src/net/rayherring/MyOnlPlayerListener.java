package net.rayherring;

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
  ResultSet result;
  
  public MyOnlPlayerListener(OnlinePlayersSQL plugin)
  {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }
  
  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerDeath(PlayerDeathEvent event)
  {
    ResultSet result = null;
    int currentDeathCount = 0;
    Player victim = event.getEntity().getPlayer();
    Player killer = event.getEntity().getKiller();
    if ((this.plugin.opConfig.trackOnlyAllowedPlayers()) && (!killer.hasPermission("onlineplayerssql.allowed"))) {
      return;
    }
    result = this.plugin.opSql.runSearchQuery("SELECT player_deaths FROM " + this.plugin.opConfig.getMySQLTable() + " WHERE player='" + victim.getName() + "';");
    
    try
    {
    	if ( result.next() ) {
    		currentDeathCount = result.getInt(1) + 1;
    	      
    	    this.plugin.opSql.SQLDisconnect();
    	      
    	    this.plugin.opSql.runUpdateQuery("UPDATE " + this.plugin.opConfig.getMySQLTable() + " SET player_deaths=" + currentDeathCount + " WHERE player='" + victim.getName() + "';");	
    	}
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    if (killer != null)
    {
      result = this.plugin.opSql.runSearchQuery("SELECT player_kills FROM " + this.plugin.opConfig.getMySQLTable() + " WHERE player='" + killer.getName() + "';");
      try
      {
        if ( result.next() ) {
        
        	currentDeathCount = result.getInt(1) + 1;
        
        	this.plugin.opSql.SQLDisconnect();
        
        	this.plugin.opSql.runUpdateQuery("UPDATE " + this.plugin.opConfig.getMySQLTable() + " SET player_kills=" + currentDeathCount + " WHERE player='" + killer.getName() + "';");
        }
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }
  }
  
  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerJoin(PlayerJoinEvent event)
  {
    int firstLogon = (int)event.getPlayer().getFirstPlayed();
    if ((this.plugin.opConfig.trackOnlyAllowedPlayers()) && (!event.getPlayer().hasPermission("onlineplayerssql.allowed"))) {
      return;
    }
    if (this.plugin.opConfig.isShowDebug()) {
      this.log.info("Player World Join: " + event.getPlayer().getName() + " " + event.getPlayer().getWorld().getName());
    }
    if (!event.getPlayer().hasPlayedBefore()) {
      this.plugin.opSql.runUpdateQuery("UPDATE " + this.plugin.opConfig.getMySQLTable() + " SET first_login=" + firstLogon + " WHERE player='" + event.getPlayer().getName() + "';");
    }
    this.plugin.updatePlayerRecord(event.getPlayer());
  }
  
  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerWorldChange(PlayerChangedWorldEvent event)
  {
    String primaryGroup = this.plugin.permission.getPrimaryGroup(event.getPlayer().getWorld().getName(), event.getPlayer().getName());
    if ((this.plugin.opConfig.trackOnlyAllowedPlayers()) && (!event.getPlayer().hasPermission("onlineplayerssql.allowed"))) {
      return;
    }
    if (this.plugin.opConfig.isShowDebug()) {
      this.log.info("Player World " + event.getPlayer().getName() + " " + event.getPlayer().getWorld().getName());
    }
    this.plugin.opSql.runUpdateQuery("UPDATE " + this.plugin.opConfig.getMySQLTable() + " SET " + 
      "previous_world='" + event.getFrom().getName() + "', " + 
      "current_world='" + event.getPlayer().getWorld().getName() + "', " + 
      "permission_group='" + primaryGroup + "' " + 
      "WHERE player='" + event.getPlayer().getName() + "';");
  }
  
  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerDisconnect(PlayerQuitEvent event)
  {
    int logoutTime = (int)(System.currentTimeMillis() / 1000L);
    if ((this.plugin.opConfig.trackOnlyAllowedPlayers()) && (!event.getPlayer().hasPermission("onlineplayerssql.allowed"))) {
      return;
    }
    if (this.plugin.opConfig.isShowDebug()) {
      this.log.info("Player Disconnected " + event.getPlayer().getName() + ".");
    }
    this.plugin.opSql.runUpdateQuery("UPDATE " + this.plugin.opConfig.getMySQLTable() + " SET " + 
      "online = false, " + 
      "last_logout = " + logoutTime + 
      " WHERE player='" + event.getPlayer().getName() + "'");
  }
}
