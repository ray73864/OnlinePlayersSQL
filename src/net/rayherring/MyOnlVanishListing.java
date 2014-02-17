package net.rayherring;

import java.util.logging.Logger;
import me.asofold.bpl.simplyvanish.api.events.SimplyVanishStateEvent;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class MyOnlVanishListing
  implements Listener
{
  Logger log = Logger.getLogger("Minecraft");
  OnlinePlayersSQL plugin;
  
  public MyOnlVanishListing(OnlinePlayersSQL plugin)
  {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }
  
  @EventHandler(priority=EventPriority.NORMAL)
  public void onVanish(SimplyVanishStateEvent event)
  {
    String sqlQuery = "";
    String logMessage = "";
    String playerName = event.getPlayer().getName();
    String sqlTable = this.plugin.opConfig.getMySQLTable();
    if (!event.getVisibleAfter())
    {
      int logonTime = (int)(System.currentTimeMillis() / 1000L);
      
      sqlQuery = "UPDATE " + sqlTable + " SET online = true, logon_time = " + logonTime + " WHERE player='" + playerName + "'";
      logMessage = "Show in stat " + playerName;
    }
    else
    {
      sqlQuery = "UPDATE " + sqlTable + " SET online = false WHERE player='" + playerName + "'";
      logMessage = "Hide in stat " + playerName;
    }
    this.plugin.opSql.runUpdateQuery(sqlQuery);
    this.log.info(logMessage);
  }
}
