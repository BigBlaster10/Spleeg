package org.trompgames.splegg;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.trompgames.utils.Updateable;

import com.connorlinfoot.titleapi.TitleAPI;

import net.md_5.bungee.api.ChatColor;

public class SpleggHandler extends Updateable{

	public static SpleggHandler handler;
	
	public static int minPlayers = 2;
	public static int maxPlayers = 8;	
	
	private GameState gameState = GameState.PREGAME;
	
	private final int COUNTDOWNSECONDS = 10;
	private int preGameSeconds = COUNTDOWNSECONDS;
	
	private final int INGAMECOUNTDOWN = 5;
	private int inGameCountdown = INGAMECOUNTDOWN;
	
	private Location lobbyLocation;
	private Location mid;
	private int y;
	
	private SpleggMap map;
	private MapVote mapVote;
	private FileConfiguration config;
	
	ArrayList<PlayerData> players = new ArrayList<PlayerData>();
	
	public SpleggHandler(Location lobbyLocation, Location mid, int y, FileConfiguration config){
		super(1);
		this.lobbyLocation = lobbyLocation;
		this.mid = mid;
		this.y = y;
		this.mapVote = new MapVote(config);
		this.config = config;
		handler = this;		
	}

	int ticks = 0;
	@Override
	protected void update() {
		ticks++;
		if(gameState.equals(GameState.PREGAME) && ticks == 20) preGameUpdate();
		else if(gameState.equals(GameState.INGAME) && ticks == 20) inGameUpdate();
		
		if(gameState.equals(GameState.INGAME)){
			for(PlayerData player : players){
				if(player.isDead()) continue;
				if(player.getPlayer().getLocation().getBlockY() <= y) killPlayer(player);
				this.checkWin();
			}
		}
		
		if(ticks >= 20) ticks = 0;	
	}
	
	public void killPlayer(PlayerData data){
		Player player = data.getPlayer();		
		player.setGameMode(GameMode.CREATIVE);
		Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " Died");
		data.setDead(true);
				
		for(PlayerData p : players){
			p.getPlayer().hidePlayer(player);
		}
		
	}
	
	public void preGameUpdate(){
		if(!(players.size() >= minPlayers)){
			if(preGameSeconds != COUNTDOWNSECONDS){
				preGameSeconds = COUNTDOWNSECONDS;
				Bukkit.broadcastMessage(ChatColor.GREEN + "Reseting countdown");
			}
			return;		
		}
		if(preGameSeconds == 30 || preGameSeconds <= 10)
			sendStartingMessage();			
		preGameSeconds--;
		if(preGameSeconds < 0) startGame();
	}
	
	public void sendStartingMessage(){
		if(preGameSeconds == 0) return;
		if(preGameSeconds == 1)
			Bukkit.broadcastMessage(ChatColor.GREEN + "Starting in " + ChatColor.GOLD + preGameSeconds + ChatColor.GREEN + " second!");
		else
			Bukkit.broadcastMessage(ChatColor.GREEN + "Starting in " + ChatColor.GOLD + preGameSeconds + ChatColor.GREEN + " seconds!");
	}
	
	public void inGameUpdate(){
		if(inGameCountdown > 0){
			gameStartingTitle();
			inGameCountdown--;
			return;
		}else if(inGameCountdown == 0){	
			gameStartTitle();
			inGameCountdown--;
		}
		
		
	}
	
	public void gameStartingTitle(){
		for(PlayerData player : players){
			TitleAPI.sendFullTitle(player.getPlayer(), 0, 25, 10, ChatColor.GREEN + "Stating in " + inGameCountdown + "...", "");
		}
	}
	
	public void gameStartTitle(){
		for(PlayerData player : players){
			TitleAPI.sendFullTitle(player.getPlayer(), 0, 10, 10, ChatColor.GREEN + "" + ChatColor.BOLD + "Splegg!", "");
		}
	}
	
	public void startGame(){
		this.gameState = GameState.INGAME;
		for(PlayerData data : players){
			Player player = data.getPlayer();
			player.teleport(mid);
			player.getInventory().addItem(new ItemStack(Material.IRON_SPADE));
		}
	}
	
	public void playerJoin(Player player){
		player.setSaturation(100000000);
		players.add(PlayerData.getPlayerData(player));
		if(gameState.equals(GameState.PREGAME)){
			player.teleport(lobbyLocation);
			Bukkit.broadcastMessage(ChatColor.GREEN + "➣ " + ChatColor.GOLD + player.getName() + ChatColor.GREEN + " has joined. " + ChatColor.GRAY + "[" + ChatColor.GOLD + players.size() + "/" + maxPlayers + ChatColor.GRAY + "]");
			mapVote.sendVotingOptions(player);		
		}
		
		else if(gameState.equals(GameState.PREGAME)) player.teleport(mid);
		
	}
	
	public void playerQuit(Player player){
		players.remove(PlayerData.getPlayerData(player));
		if(gameState.equals(GameState.PREGAME)){
			Bukkit.broadcastMessage(ChatColor.RED + "✘ " + ChatColor.GOLD + player.getName() + ChatColor.GREEN + " has left. " + ChatColor.GRAY + "[" + ChatColor.GOLD + players.size() + "/" + maxPlayers + ChatColor.GRAY + "]");
		}
	}
	
	public ArrayList<PlayerData> getAllivePlayers(){
		ArrayList<PlayerData> allivePlayers = new ArrayList<PlayerData>();
		for(PlayerData player : players){
			if(!player.isDead()) allivePlayers.add(player);
		}
		return allivePlayers;
	}
	
	public void checkWin(){
		if(getAllivePlayers().size() == 1){
			win(getAllivePlayers().get(0));
		}
	}
	
	public void win(PlayerData player){
		gameState = GameState.OVER;
		for(PlayerData data : players){
			TitleAPI.sendFullTitle(data.getPlayer(), 10, 120, 20, ChatColor.GOLD + "" + ChatColor.BOLD + player.getPlayer().getName() + ChatColor.GREEN + " has won!", "");
		}
	}
	
	
	public static enum GameState{
		PREGAME,
		INGAME,
		OVER;		
	}
	
}
