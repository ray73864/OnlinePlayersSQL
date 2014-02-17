package net.rayherring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class OnlinePlayersSQLLib
{
  Logger log = Logger.getLogger("Minecraft");
  String url;
  Connection conn = null;
  PreparedStatement myQuery = null;
  OnlinePlayersSQL plugin;
  
  public OnlinePlayersSQLLib(OnlinePlayersSQL plugin)
  {
    this.plugin = plugin;
    this.url = ("jdbc:mysql://" + plugin.opConfig.getMySQLServer() + ":" + plugin.opConfig.getMySQLPort() + "/" + plugin.opConfig.getMySQLDatabase());
  }
  
  public Connection SQLConnect()
    throws SQLException
  {
    Connection conn = DriverManager.getConnection(this.url, this.plugin.opConfig.getMySQLUsername(), this.plugin.opConfig.getMySQLPassword());
    return conn;
  }
  
  public void SQLDisconnect()
    throws SQLException
  {
    this.myQuery.close();
    this.conn.close();
  }
  
  public void updateTableSchema()
    throws SQLException
  {
    this.log.info("Updating Schema information for table.");
    if (!columnExists(this.plugin.opConfig.getMySQLDatabase(), this.plugin.opConfig.getMySQLTable(), "online"))
    {
      this.log.info("Creating additional 'online' column for table.");
      runUpdateQuery("ALTER TABLE " + this.plugin.opConfig.getMySQLTable() + " ADD COLUMN online boolean default false;");
    }
    if (!columnExists(this.plugin.opConfig.getMySQLDatabase(), this.plugin.opConfig.getMySQLTable(), "last_logout"))
    {
      this.log.info("Creating additional 'last_logout' column for table.");
      runUpdateQuery("ALTER TABLE " + this.plugin.opConfig.getMySQLTable() + " ADD COLUMN last_logout int;");
    }
    if (!columnExists(this.plugin.opConfig.getMySQLDatabase(), this.plugin.opConfig.getMySQLTable(), "first_login"))
    {
      this.log.info("Creating additional 'first_login' column for table.");
      runUpdateQuery("ALTER TABLE " + this.plugin.opConfig.getMySQLTable() + " ADD COLUMN first_login int;");
    }
    if (!columnExists(this.plugin.opConfig.getMySQLDatabase(), this.plugin.opConfig.getMySQLTable(), "player_deaths"))
    {
      this.log.info("Creating additional 'player_deaths' column for table.");
      runUpdateQuery("ALTER TABLE " + this.plugin.opConfig.getMySQLTable() + " ADD COLUMN player_deaths int; ");
    }
    if (!columnExists(this.plugin.opConfig.getMySQLDatabase(), this.plugin.opConfig.getMySQLTable(), "player_kills"))
    {
      this.log.info("Creating additional 'player_kills' column for table.");
      runUpdateQuery("ALTER TABLE " + this.plugin.opConfig.getMySQLTable() + " ADD COLUMN player_kills int; ");
    }
  }
  
  public void runUpdateQuery(String query)
  {
    try
    {
      this.conn = SQLConnect();
      this.myQuery = this.conn.prepareStatement(query);
      
      this.myQuery.executeUpdate();
      SQLDisconnect();
    }
    catch (SQLException e1)
    {
      e1.printStackTrace();
    }
  }
  
  public void runUpdateQueryNew(OnlinePlayersSQLQuery query) {
	  
	  PreparedStatement statement = null;
	  HashMap<Integer, Object> params = null;
	  int numberOfParams = 0;
	  
	  try {
		  this.conn = SQLConnect();
		  
		  this.conn.setAutoCommit(false);
		  statement = this.conn.prepareStatement(query.getQuery());
		  
		  params = query.getParams();
		  numberOfParams = query.numberOfParams();
		  
		  for ( int i = 0; i < numberOfParams; i++ ) {
			  if (params.get(i) instanceof Integer) statement.setInt(i+1, (int) params.get(i));
			  if (params.get(i) instanceof String) statement.setString(i+1, (String) params.get(i));
			  if (params.get(i) instanceof Boolean) statement.setBoolean(i+1, (boolean) params.get(i));
		  }
		  
		  log.info(statement.toString());
		  
		  statement.executeUpdate();
		  this.conn.commit();
		  
		  this.conn.setAutoCommit(true);
		  
	  } catch (SQLException e) {
		  e.printStackTrace();
	  }
  }
  
  public ResultSet runSearchQuery(String query)
  {
    ResultSet result = null;
    try
    {
      this.conn = SQLConnect();
      this.myQuery = this.conn.prepareStatement(query);
      
      result = this.myQuery.executeQuery();
    }
    catch (SQLException el)
    {
      el.printStackTrace();
    }
    return result;
  }
  
  public void createSqlTable()
    throws SQLException
  {
    runUpdateQuery("CREATE TABLE " + this.plugin.opConfig.getMySQLTable() + 
      "(player varchar(255) not null, " + 
      "previous_world varchar(255), " + 
      "current_world varchar(255), " + 
      "ip_address varchar(16), " + 
      "logon_time int(11), " + 
      "permission_group varchar(255), " + 
      "online boolean default false, " + 
      "last_logout int(11), " + 
      "first_login int(11))");
  }
  
  public boolean tableExists(String db, String tbl)
  {
    ResultSet result = null;
    Boolean recordExists = Boolean.valueOf(false);
    
    String query = "SELECT * FROM Information_Schema.TABLES WHERE Information_Schema.TABLES.TABLE_NAME = '" + 
      tbl + "' " + 
      "AND Information_Schema.TABLES.TABLE_SCHEMA = '" + db + "'";
    
    result = runSearchQuery(query);
    try
    {
      recordExists = Boolean.valueOf(result.isBeforeFirst());
      SQLDisconnect();
      
      return recordExists.booleanValue();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  public boolean columnExists(String db, String tbl, String column)
  {
    ResultSet result = null;
    Boolean recordExists = Boolean.valueOf(false);
    
    String query = "SELECT * FROM Information_Schema.COLUMNS WHERE Information_Schema.COLUMNS.COLUMN_NAME = '" + 
      column + "' " + 
      "AND Information_Schema.COLUMNS.TABLE_NAME = '" + tbl + "' " + 
      "AND Information_Schema.COLUMNS.TABLE_SCHEMA = '" + db + "'";
    

    result = runSearchQuery(query);
    try
    {
      this.log.info("Result of column " + column + " check: " + result.isBeforeFirst());
      
      recordExists = Boolean.valueOf(result.isBeforeFirst());
      
      SQLDisconnect();
      
      return recordExists.booleanValue();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return false;
  }
}
