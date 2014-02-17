package net.rayherring;

public class OnlinePlayersSQLConfig {

	OnlinePlayersSQL plugin;
	
	public OnlinePlayersSQLConfig(OnlinePlayersSQL onlinePlayersSQL) {
		this.plugin = onlinePlayersSQL;
	}

	public void loadConfiguration() {
		String mySQLServer = "MySQLServer";
		String mySQLPort = "MySQLPort";
		String mySQLUsername = "MySQLUsername";
		String mySQLPassword = "MySQLPassword";
		String mySQLDatabase = "MySQLDatabase";
		String mySQLTable = "MySQLTable";
		
		this.plugin.getConfig().addDefault(mySQLServer, "localhost");
		this.plugin.getConfig().addDefault(mySQLPort, "3306");
		this.plugin.getConfig().addDefault(mySQLUsername,  "root");
		this.plugin.getConfig().addDefault(mySQLPassword, "");
		this.plugin.getConfig().addDefault(mySQLDatabase, "db");
		this.plugin.getConfig().addDefault(mySQLTable, "online_players");
		this.plugin.getConfig().addDefault("showDebug", Boolean.valueOf(false));
		this.plugin.getConfig().addDefault("op_only_resync", Boolean.valueOf(false));
		this.plugin.getConfig().addDefault("track_only_allowed_players", Boolean.valueOf(false));
		
		this.plugin.getConfig().options().copyDefaults(true);
		
		this.plugin.saveConfig();
	}

	public String getMySQLTable() {
		return this.plugin.getConfig().getString("MySQLTable");
	}

	public String getMySQLDatabase() {
		return this.plugin.getConfig().getString("MySQLDatabase");
	}

	public boolean trackOnlyAllowedPlayers() {
		return this.plugin.getConfig().getBoolean("track_only_allowed_players");
	}

	public boolean isShowDebug() {
		return this.plugin.getConfig().getBoolean("showDebug");
	}

	public boolean opOnlyResync() {
		return this.plugin.getConfig().getBoolean("op_only_resync");
	}
	
	public String getMySQLPassword() {
		return this.plugin.getConfig().getString("MySQLPassword");
	}

	public String getMySQLPort() {
		return this.plugin.getConfig().getString("MySQLPort");
	}
	
	public String getMySQLServer() {
		return this.plugin.getConfig().getString("MySQLServer");
	}

	public String getMySQLUsername() {
		return this.plugin.getConfig().getString("MySQLUsername");
	}
}
