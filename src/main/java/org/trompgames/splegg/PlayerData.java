package main.java.org.trompgames.splegg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import main.java.org.trompgames.utils.Updateable;

public class PlayerData extends Updateable{

	public static final int SHOOTCOOLDOWN = 2;

	private SpleggHandler handler;
	private Player player;
	private boolean isDead = false;
	
	private boolean hasVoted = false;
	
	private int cooldown = 0;
	
	private PlayerStats playerStats;
	
	private static List<PlayerData> playerData = new ArrayList<>();
	
	private PlayerData(Player player){
		super(1);
		this.player = player;
		this.playerStats = new PlayerStats(player);
		playerData.add(this);
	}
	
	public static PlayerData getPlayerData(Player player){
		for(PlayerData data : playerData){
			if(data.getPlayer().equals(player)) return data;
		}
		return new PlayerData(player);
	}	
	
	public static boolean hasData(Player player){
		for(PlayerData data : playerData){
			if(data.getPlayer().equals(player)) return true;
		}
		return false;
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public boolean isDead(){
		return isDead;
	}
	
	public boolean hasVoted(){
		return hasVoted;
	}
	
	public void setVoted(boolean hasVoted){
		this.hasVoted = hasVoted;
	}
	
	public SpleggHandler getSpleggHandler(){
		return handler;
	}
	
	public boolean isInGame(){
		if(handler == null) return false;
		return true;
	}
	
	public void setSpleggHandler(SpleggHandler handler){
		this.handler = handler;
	}
	
	public void setDead(boolean isDead){
		this.getPlayerStats().addDeath();
		player.getWorld().strikeLightning(player.getLocation());
		
		this.isDead = isDead;
	}

	public boolean canShoot(){
		return cooldown <= 0;
	}
	
	public void shoot(){
		this.cooldown = SHOOTCOOLDOWN;
		this.playerStats.addEggsShot();
	}
	
	public PlayerStats getPlayerStats(){
		return playerStats;
	}
	
	@Override
	protected void update() {
		if(cooldown > 0){
			cooldown--;		
		}
	}
	
	
	public static List<PlayerData> getPlayerData() {
		if (playerData != null) {
			return playerData;
		}
		throw new NullPointerException("PlayerData not initialized.");
	}	
	
	public static class PlayerStats{
		
		private Player player;
		
		private int deaths;
		private int gamesPlayed;
		private int eggsShot;
		private int blocksDestroyed;
		private int points;		
		private int wins;
		
		private int currentDeaths;
		private int currentGamesPlayed;
		private int currentEggsShot;
		private int currentBlocksDestroyed;
		private int currentPoints;		
		private int currentWins;
		
	

		public static Connection conn;
		public static String table;
		public static String schema;
		
		public static ArrayList<PlayerStats> stats = new ArrayList<PlayerStats>();
		
		private PlayerStats(Player player){
			this.player = player;
			stats.add(this);
			loadStats();
		}
		
		private void loadStats(){
			String query = "SELECT * FROM " + table + " WHERE PlayerUUID = '" + player.getUniqueId().toString() +  "'";
			Statement stmt;			
			ResultSet rs;
			
			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query);
				if(rs.next()){
					if(!rs.getString("PlayerName").equals(player.getName())) PlayerStats.updatePlayerName(player);			
					this.currentDeaths = rs.getInt("Deaths");
					this.currentGamesPlayed = rs.getInt("GamesPlayed");
					this.currentEggsShot = rs.getInt("EggsShot");
					this.currentBlocksDestroyed = rs.getInt("BlocksDestroyed");
					this.currentPoints = rs.getInt("Points");
					this.currentWins = rs.getInt("Wins");					
				}else{
					String prep = "INSERT INTO " + table + " VALUES ('" + player.getName() + "', '" + player.getUniqueId().toString() + "', 0, 0, 0, 0, 0, 0);";
					try {
						PreparedStatement pStmt = conn.prepareStatement(prep);
						pStmt.executeUpdate();
						pStmt.close();  
					} catch (SQLException e) {
						e.printStackTrace();
					}			
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		
		
		
		public static PlayerStats getPlayer(Player player){
			for(PlayerStats stat : stats){
				if(stat.getPlayer().equals(player)) return stat;
			}
			return new PlayerStats(player);
		}			
		
		public static void createConnection(String url, String user, String pass, String schema, String table){
			PlayerStats.schema = schema;
			PlayerStats.table = table;
			try {
				conn = DriverManager.getConnection(url, user, pass);				
				createTable(conn);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}		
		
		private static void updatePlayerName(Player player){		
			String query = "UPDATE " + table + " SET PlayerName = '" + player.getName() + "' WHERE PlayerUUID = '" + player.getUniqueId().toString() + "';";
			try {
				PreparedStatement pStmt = conn.prepareStatement(query);
				pStmt.executeUpdate();
				pStmt.close();  
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}	
		
		private static void createTable(Connection conn){
			String create = "CREATE TABLE IF NOT EXISTS `" + table + "` (`PlayerName` VARCHAR(30) NULL,`PlayerUUID` VARCHAR(45) NULL,`Wins` INT NULL,`GamesPlayed` INT NULL,`EggsShot` INT NULL, `BlocksDestroyed` INT NULL,`Points` INT NULL,`Deaths` INT NULL);";
			try {
				PreparedStatement pStmt = conn.prepareStatement(create);
				pStmt.executeUpdate();
				pStmt.close();  
			} catch (SQLException e) {
				e.printStackTrace();
			}			
			
		}
		
		public static void saveStats(){			
			for(PlayerStats ps : stats){
				ps.loadStats();
				
				String[] qs = new String[6];
				String uuid = ps.player.getUniqueId().toString();			
				
				qs[0] = "UPDATE " + table + " SET Deaths = " + (ps.deaths + ps.currentDeaths )+ " WHERE PlayerUUID = '" + uuid + "';";
				qs[1] = "UPDATE " + table + " SET GamesPlayed = " + (ps.gamesPlayed + ps.currentGamesPlayed) + " WHERE PlayerUUID = '" + uuid + "';";
				qs[2] = "UPDATE " + table + " SET EggsShot = " + (ps.eggsShot + ps.currentEggsShot) + " WHERE PlayerUUID = '" + uuid + "';";
				qs[3] = "UPDATE " + table + " SET BlocksDestroyed = " + (ps.blocksDestroyed + ps.currentBlocksDestroyed) + " WHERE PlayerUUID = '" + uuid + "';";
				qs[4] = "UPDATE " + table + " SET Points = " + (ps.points + ps.currentPoints) + " WHERE PlayerUUID = '" + uuid + "';";
				qs[5] = "UPDATE " + table + " SET Wins = " + (ps.wins + ps.currentWins) + " WHERE PlayerUUID = '" + uuid + "';";

				for(String q : qs){
					try {
						PreparedStatement pStmt = conn.prepareStatement(q);
						pStmt.executeUpdate();
						pStmt.close();  
					} catch (SQLException e) {
						e.printStackTrace();
					}					
				}
				
			}
			
			
			
		}
		
		
		
		/*
		 * 
  CREATE TABLE `splegg`.`splegg` (
  `PlayerName` VARCHAR(30) NULL,
  `PlayerUUID` VARCHAR(45) NULL,
  `Wins` INT NULL,
  `GamesPlayed` INT NULL,
  `EggsShot` INT NULL,
  `BlocksDestroyed` INT NULL,
  `Points` INT NULL);
		 */
		
		
		
		
		
		
		public Player getPlayer(){
			return player;
		}
		
		
		public void addDeath(){
			deaths++;
		}
		
		public void addGamePlayed(){
			gamesPlayed++;
		}
		
		public void addEggsShot(){
			eggsShot++;
		}
		
		public void addBlocksDestroyed(){
			blocksDestroyed++;
		}
		
		public void addPoints(int points){
			points += points;
		}
		
		public void addWin(){
			wins++;
		}

		public int getDeaths() {
			return deaths;
		}

		public int getGamesPlayed() {
			return gamesPlayed;
		}

		public int getEggsShot() {
			return eggsShot;
		}

		public int getBlocksDestroyed() {
			return blocksDestroyed;
		}

		public int getPoints() {
			return points;
		}

		public int getWins() {
			return wins;
		}

		public static ArrayList<PlayerStats> getPlayerStats() {
			return stats;
		}		
		
		public int getCurrentDeaths() {
			return currentDeaths;
		}

		public int getCurrentGamesPlayed() {
			return currentGamesPlayed;
		}

		public int getCurrentEggsShot() {
			return currentEggsShot;
		}

		public int getCurrentBlocksDestroyed() {
			return currentBlocksDestroyed;
		}

		public int getCurrentPoints() {
			return currentPoints;
		}

		public int getCurrentWins() {
			return currentWins;
		}
	}
	
	
	

}
