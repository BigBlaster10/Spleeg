
package main.java.org.trompgames.splegg;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.connorlinfoot.titleapi.TitleAPI;

import main.java.org.trompgames.utils.MapVote;
import main.java.org.trompgames.utils.Schematic;
import main.java.org.trompgames.utils.SpleggMap;
import main.java.org.trompgames.utils.Updateable;


public class SpleggHandler extends Updateable{

	public static int minPlayers = 2;
	public static int maxPlayers = 8;	
	
	private GameState gameState = GameState.PREGAME;
	
	private final int COUNTDOWNSECONDS = 20;
	private int preGameSeconds = COUNTDOWNSECONDS;
	
	private final int INGAMECOUNTDOWN = 5;
	private int inGameCountdown = INGAMECOUNTDOWN;
	
	private World world;
	private Location lobbyLocation;
	private Location mid;
	private int y;
	
	private SpleggMap map;
	private MapVote mapVote;
	private FileConfiguration config;
    private ConfigMessage configMessaage;

	private SpleggMain plugin; 
	
	ArrayList<PlayerData> players = new ArrayList<>();
	
	protected SpleggHandler(Location lobbyLocation, Location mid, int y, FileConfiguration config, SpleggMain plugin, ConfigMessage configMessage){
		super(1);
		this.lobbyLocation = lobbyLocation;
		this.world = lobbyLocation.getWorld();
		this.mid = mid;
		this.y = y;
		this.mapVote = new MapVote(config, configMessage);
		this.config = config;
		this.plugin = plugin;
		this.configMessaage = configMessage;
		Schematic.loadArea(plugin, world, new File("plugins\\WorldEdit\\schematics\\clear.schematic"), mid, true);

	}

	private int ticks = 0;
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
	
	public void checkWin(){
		if(getAllivePlayers().size() == 1){
			win(getAllivePlayers().get(0));
		}
	}

	public ConfigMessage getConfigMessage(){
		return this.configMessaage;
	}
	
	public void killPlayer(PlayerData data){
		Player player = data.getPlayer();		
		player.setGameMode(GameMode.CREATIVE);
		//Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " Died");
		Bukkit.broadcastMessage(this.getConfigMessage().getMessage(player, "game.playerDeath"));

		data.setDead(true);
				
		for(PlayerData p : players){
			p.getPlayer().hidePlayer(player);
		}
	}

	
	public void preGameUpdate(){
		if(!(players.size() >= minPlayers)){
			if(preGameSeconds != COUNTDOWNSECONDS){
				preGameSeconds = COUNTDOWNSECONDS;
				//Bukkit.broadcastMessage(ChatColor.GREEN + "Reseting countdown");
				Bukkit.broadcastMessage(this.getConfigMessage().getMessage("game.resetCountdown"));

			}
			return;		
		}
		if(preGameSeconds == 10){
			map = mapVote.getWinner(world);
			map.loadMap(plugin, mid);
			Bukkit.broadcastMessage(this.getConfigMessage().getMessage(map.getMapName(), "game.mapSelected"));

			//Bukkit.broadcastMessage(ChatColor.GREEN + "The map " + ChatColor.GOLD + map.getMapName() + ChatColor.GREEN + " has been selected!");
		}
		if(preGameSeconds == 30 || preGameSeconds <= 10)
			sendStartingMessage();			
		preGameSeconds--;
		if(preGameSeconds < 0) startGame();
	}
	
	public void sendStartingMessage(){
		if(preGameSeconds == 0) return;
		for(PlayerData data : players){
			Player player = data.getPlayer();
			player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1f);
		}
		if(preGameSeconds == 1)
			//Bukkit.broadcastMessage(ChatColor.GREEN + "Starting in " + ChatColor.GOLD + preGameSeconds + ChatColor.GREEN + " second!");
			Bukkit.broadcastMessage(this.getConfigMessage().getMessage("game.lastGameStartingMessage"));

		else
			//Bukkit.broadcastMessage(ChatColor.GREEN + "Starting in " + ChatColor.GOLD + preGameSeconds + ChatColor.GREEN + " seconds!");
			Bukkit.broadcastMessage(this.getConfigMessage().getMessage(preGameSeconds, "game.gameStartingMessage"));

	}

	
	public void inGameUpdate(){
		if(inGameCountdown > 0){
			gameStartingTitle();
			inGameCountdown--;
		}else if(inGameCountdown == 0){
			gameStartTitle();
			for(PlayerData data : players){
				Player player = data.getPlayer();
				player.getInventory().addItem(new ItemStack(Material.IRON_SPADE));
			}
			inGameCountdown--;
		}
		
		
	}
	
	public void gameStartingTitle(){
		for(PlayerData player : players){
			String title = this.getConfigMessage().getMessage(this.inGameCountdown, "game.gameStartingTitle");

			TitleAPI.sendFullTitle(player.getPlayer(), 0, 25, 10, title, "");
		}
	}
	
	public void gameStartTitle(){
		for(PlayerData player : players){
			String title = this.getConfigMessage().getMessage("game.gameStartTitle");

			TitleAPI.sendFullTitle(player.getPlayer(), 0, 10, 10, ChatColor.GREEN + "" + ChatColor.BOLD + "Splegg!", "");
		}
	}

	
	public void startGame(){
		this.gameState = GameState.INGAME;
		for(PlayerData data : players){
			Player player = data.getPlayer();
			player.teleport(mid);
		}
	}
	
	public void playerJoin(Player player){
		player.setSaturation(100000000);
		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		PlayerData data = PlayerData.getPlayerData(player);
		players.add(data);
		if(gameState.equals(GameState.PREGAME)){
			player.teleport(lobbyLocation);
			Bukkit.broadcastMessage(this.getConfigMessage().getMessage(player, "game.playerJoin"));
			//Bukkit.broadcastMessage(ChatColor.GREEN + "➣ " + ChatColor.GOLD + player.getName() + ChatColor.GREEN + " has joined. " + ChatColor.GRAY + "[" + ChatColor.GOLD + players.size() + "/" + maxPlayers + ChatColor.GRAY + "]");
			mapVote.sendVotingOptions(data, this);	
		}else if(gameState.equals(GameState.PREGAME)) player.teleport(mid);
	}
	
	public void playerQuit(Player player){
		players.remove(PlayerData.getPlayerData(player));
		if(gameState.equals(GameState.PREGAME)){
			Bukkit.broadcastMessage(this.getConfigMessage().getMessage(player, "game.playerQuit"));

			//Bukkit.broadcastMessage(ChatColor.RED + "✘ " + ChatColor.GOLD + player.getName() + ChatColor.GREEN + " has left. " + ChatColor.GRAY + "[" + ChatColor.GOLD + players.size() + "/" + maxPlayers + ChatColor.GRAY + "]");
		}
	}
	
	public void playerVote(Player player, int number){
		PlayerData data = PlayerData.getPlayerData(player);
		mapVote.playerVote(player, number, this);
	}
	
	public SpleggMap getMap(){
		return this.map;
	}
	
	public GameState getGameState(){
		return gameState;
	}
	
	public MapVote getMapVote(){
		return mapVote;
	}
	
	public ArrayList<PlayerData> getAllivePlayers(){
		return players.stream().filter(player -> !player.isDead()).collect(Collectors.toCollection(ArrayList::new));
	}
	
	public ArrayList<PlayerData> getPlayers(){
		return players;
	}
	
	public static int getMaxPlayers(){
		return maxPlayers;
	}
	
	public static int getMinPlayers(){
		return minPlayers;
	}
	
	public void win(PlayerData player){
		gameState = GameState.OVER;
		String title = this.getConfigMessage().getMessage(player.getPlayer(), "game.playerWinTitle");
		player.getPlayer().setGameMode(GameMode.CREATIVE);
		for(PlayerData data : players){
			
			TitleAPI.sendFullTitle(data.getPlayer(), 10, 120, 20, title, "");
		}
		
		for(PlayerData data : players){
			for(Player p : Bukkit.getOnlinePlayers()){
				if(!p.equals(data.getPlayer()))
					p.showPlayer(data.getPlayer());
			}
		}
		
		
	}


	public enum GameState{
		PREGAME,
		INGAME,
		OVER;
	}
	
}

