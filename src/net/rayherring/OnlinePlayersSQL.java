package net.rayherring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class OnlinePlayersSQL extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");
	public final OnlinePlayersSQLConfig opConfig = new OnlinePlayersSQLConfig(this);
	public OnlinePlayersSQLLib opSql = null;
	public Permission permission = null;
	
	public void onEnable() {
		PluginDescriptionFile pdf = getDescription();
		
		this.opConfig.loadConfiguration();
		
		this.opSql = new OnlinePlayersSQLLib(this);
		
		this.log.info("[" + pdf.getName() + "] " + pdf.getVersion() + " enabled");
		
		new MyOnlPlayerListener(this);
		
		if ( Bukkit.getPluginManager().getPlugin("SimplyVanish") != null) {
			new MyOnlVanishListing(this);
		}
		
		try {
			if ( !this.opSql.tableExists(this.opConfig.getMySQLDatabase(), this.opConfig.getMySQLTable())) {
				System.out.println("Table '" + this.opConfig.getMySQLTable() + "' does not exist! Creating table...");
				this.opSql.createSqlTable();
				System.out.println("Table '" + this.opConfig.getMySQLTable() + "' created!");
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		try {
			this.opSql.updateTableSchema();
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		
		OnlinePlayersSQLQuery myQuery = new OnlinePlayersSQLQuery("UPDATE " + this.opConfig.getMySQLTable() + " SET online = ?", false);
		this.opSql.runUpdateQueryNew(myQuery);
		
		setupPermissions();
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if ( permissionProvider != null ) {
			this.permission = ((Permission)permissionProvider.getProvider());
		}
		
		return this.permission != null;
	}
	
	public void onDisable() {
		PluginDescriptionFile pdf = getDescription();
		this.log.info(pdf.getName() + " " + pdf.getVersion() + " disabled");
	}
	
	public void updatePlayerRecord(Player player) {
		String primaryGroup = null;
		Player thisPlayer = player.getPlayer();
		String playerName = thisPlayer.getName();
		String playerWorld = thisPlayer.getWorld().getName();
		String ipAddress = thisPlayer.getAddress().getAddress().getHostAddress();
		String sqlTable = this.opConfig.getMySQLTable();
		int logonTime = (int)(System.currentTimeMillis() / 1000L);
		int logonTime2 = (int)thisPlayer.getPlayerTime();
		Boolean recordExists = Boolean.valueOf(false);
		OnlinePlayersSQLQuery myQuery = null;
		Connection conn = null;
		
		ResultSet result = null;
		
		if ((this.opConfig.trackOnlyAllowedPlayers()) && (!player.hasPermission("onlineplayerssql.allowed"))) {
			return;
		}

		if ( this.opConfig.isShowDebug()) {
			this.log.info("Player: " + playerName + " Logon Time: " + logonTime2);
		}

		myQuery = new OnlinePlayersSQLQuery("SELECT * FROM " + sqlTable + " WHERE player = ?", playerName);
		PreparedStatement playerExists = null;
		
		try {
			conn = opSql.SQLConnect();
			playerExists = conn.prepareStatement(myQuery.getQuery());
			result = this.opSql.runSearchQueryNew(conn, myQuery, playerExists);
			
			boolean hasNextRow = result.next();

			if ( hasNextRow ) {
				myQuery = new OnlinePlayersSQLQuery(
						"UPDATE " + sqlTable + " SET online = ?, current_world = ?, ip_address = ?, logon_time = ? WHERE player = ?"
							, true, playerWorld, ipAddress, logonTime, playerName
				);
				
			} else {
				myQuery = new OnlinePlayersSQLQuery(
						"INSERT INTO " + sqlTable + " ( player, current_world, ip_address, logon_time, online ) VALUES ( ?, ?, ?, ?, ? )"
							, playerName, playerWorld, ipAddress, logonTime, true
				);
			}
			
			this.opSql.runUpdateQueryNew(myQuery);
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try { conn.close(); } catch (SQLException e) { }
		}
		
		if (this.permission != null) {
			try {
				primaryGroup = this.permission.getPrimaryGroup(player.getPlayer().getWorld().getName(), player.getPlayer().getName());
				
				myQuery = new OnlinePlayersSQLQuery(
						"UPDATE " + sqlTable + " SET permission_group = ? WHERE player = ?",
							primaryGroup, player.getPlayer().getName());
				
				this.opSql.runUpdateQueryNew(myQuery);
			} catch ( UnsupportedOperationException e) {
				
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		String sqlTable = this.opConfig.getMySQLTable();
		
		if ((sender instanceof Player)) {
			player = (Player)sender;
			
			if ( args.length == 0 ) {
				return false;
			}
			
			if ( command.getName().equalsIgnoreCase("onl")) {
				if ( args[0].equalsIgnoreCase("resync")) {
					if ((this.opConfig.opOnlyResync()) && (!player.isOp())) {
						player.sendMessage("Only opped players can use this command.");
						return false;
					}
					
					OnlinePlayersSQLQuery myQuery = new OnlinePlayersSQLQuery("UPDATE " + sqlTable + " SET online = ?", false);					
					this.opSql.runUpdateQueryNew(myQuery);
					
					Player[] players = getServer().getOnlinePlayers();
					for ( int i = 0; i < players.length; i++) {
						updatePlayerRecord(players[i]);
					}
					return true;
				} else {
					sender.sendMessage("Invalid /onl option. Valid options: resync");
				}
			} else {
				sender.sendMessage("Invalid /onl option. Valid options: resync");
			}
		}
		
		return false;
	}
	
}
