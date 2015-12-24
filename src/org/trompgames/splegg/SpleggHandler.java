package org.trompgames.splegg;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
	
	ArrayList<PlayerData> players = new ArrayList<PlayerData>();
	
	public SpleggHandler(Location lobbyLocation, Location mid, int y){
		super(1);
		this.lobbyLocation = lobbyLocation;
		this.mid = mid;
		this.y = y;
		handler = this;		
	}

	int ticks = 0;
	@Override
	protected void update() {
		ticks++;
		if(gameState.equals(GameState.PREGAME) && ticks == 20) preGameUpdate();
		else if(gameState.equals(GameState.INGAME) && ticks == 20) inGameUpdate();
		if(ticks >= 20) ticks = 0;
		for(PlayerData player : players){
			if(player.isDead()) continue;
			if(player.getPlayer().getLocation().getBlockY() <= y) killPlayer(player);
		}
		
	}
	
	public void killPlayer(PlayerData data){
		Player player = data.getPlayer();		
		player.setGameMode(GameMode.CREATIVE);
		Bukkit.broadcastMessage(player.getName() + " Died");
		data.setDead(true);
				
		for(PlayerData p : players){
			p.getPlayer().hidePlayer(player);
		}
		
	}
	
	public void preGameUpdate(){
		if(!(players.size() >= minPlayers)) return;		
		if(preGameSeconds == 30 || preGameSeconds <= 10)
			sendStartingMessage();			
		preGameSeconds--;
		if(preGameSeconds <= 0) startGame();
	}
	
	public void sendStartingMessage(){
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
			TitleAPI.sendFullTitle(player.getPlayer(), 0, 10, 10, ChatColor.GREEN + "Stating in " + inGameCountdown + "...", "");
		}
	}
	
	public void gameStartTitle(){
		for(PlayerData player : players){
			TitleAPI.sendFullTitle(player.getPlayer(), 0, 10, 10, ChatColor.GREEN + "" + ChatColor.BOLD + "Splegg!", "");
		}
	}
	
	public void startGame(){
		this.gameState = GameState.INGAME;
		for(PlayerData player : players){
			player.getPlayer().teleport(mid);
		}
	}
	
	public void playerJoin(Player player){
		player.setSaturation(100000000);
		players.add(PlayerData.getPlayerData(player));
		if(gameState.equals(GameState.PREGAME)) player.teleport(lobbyLocation);
		else if(gameState.equals(GameState.PREGAME)) player.teleport(mid);
	}
	
	public void playerQuit(Player player){
		players.remove(PlayerData.getPlayerData(player));
	}
	
	public ArrayList<PlayerData> getAllivePlayer(){
		ArrayList<PlayerData> allivePlayers = new ArrayList<PlayerData>();
		for(PlayerData player : players){
			if(!player.isDead()) allivePlayers.add(player);
		}
		return allivePlayers;
	}
	
	
	public static enum GameState{
		PREGAME,
		INGAME;		
	}
	
}
