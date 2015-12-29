
package main.java.org.trompgames.splegg;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
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
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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
	
	private final int INGAMETIME = 600 + INGAMECOUNTDOWN + 1;
	private int inGameTime = INGAMETIME;
	
	private World world;
	private Location lobbyLocation;
	private Location mid;
	private int y;
	
	private SpleggMap map;
	private MapVote mapVote;
	private FileConfiguration config;
    private ConfigMessage configMessage;

    private SpleggScoreboard spleggScoreboard;
    
	private SpleggMain plugin; 
	
	ArrayList<PlayerData> players = new ArrayList<>();
	
	public void restart(){
		sendMessage(this.configMessage.getMessage("game.gameRestart"));
		PlayerData.PlayerStats.saveStats();
		PlayerData.getPlayerData().clear();
		gameState = GameState.PREGAME;

		ArrayList<PlayerData> datas = (ArrayList<PlayerData>) players.clone();
		players.clear();
		
		if(config.getBoolean("bungee.enabled") && config.getBoolean("bungee.kickEnabled")){
			for(Player player: Bukkit.getOnlinePlayers()){
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
	            out.writeUTF("Connect");
	            out.writeUTF(config.getString("bungee.kickServer")); 
	            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());				
			}	
		}else{
			for(PlayerData data : datas){
				this.playerJoin(data.getPlayer());				
			}
		}		
		
		map = null;
		this.preGameSeconds = COUNTDOWNSECONDS;
		this.inGameCountdown = INGAMECOUNTDOWN;
		this.inGameTime = INGAMETIME;
		this.won = false;
		this.mapVote = new MapVote(config, configMessage);
		this.spleggScoreboard = new PreGameScoreboard(this, configMessage);
		for(PlayerData data : players){
			spleggScoreboard.addPlayer(data.getPlayer());
		}
		Schematic.loadArea(plugin, world, new File("plugins\\WorldEdit\\schematics\\clear.schematic"), mid, true);
	}
	
	protected SpleggHandler(Location lobbyLocation, Location mid, int y, FileConfiguration config, SpleggMain plugin, ConfigMessage configMessage){
		super(1);
		this.lobbyLocation = lobbyLocation;
		this.world = lobbyLocation.getWorld();
		this.mid = mid;
		this.y = y;
		this.mapVote = new MapVote(config, configMessage);
		this.config = config;
		this.plugin = plugin;
		this.configMessage = configMessage;
		this.spleggScoreboard = new PreGameScoreboard(this, configMessage);
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
		spleggScoreboard.updateScoreboard();
		if(ticks >= 20) ticks = 0;
	}
	
	
	
	public void checkWin(){
		if(getAllivePlayers().size() == 1){
			win(getAllivePlayers().get(0));
		}
	}

	public ConfigMessage getConfigMessage(){
		return this.configMessage;
	}
	
	public ArrayList<PlayerData> getSpectators(){
		ArrayList<PlayerData> specs = new ArrayList<>();
		for(PlayerData data : players){
			if(data.isDead() && Bukkit.getPlayer(data.getPlayer().getName()) != null) specs.add(data);
		}
		return specs;
	}
	
	public void killPlayer(PlayerData data){
		Player player = data.getPlayer();		
		player.setGameMode(GameMode.CREATIVE);
		//Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " Died");
		sendMessage(this.getConfigMessage().getMessage(player, "game.playerDeath"));
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
				sendMessage(this.getConfigMessage().getMessage("game.resetCountdown"));

			}
			return;		
		}
		if(preGameSeconds == 10){
			map = mapVote.getWinner(world);
			map.loadMap(plugin, mid);
			sendMessage(this.getConfigMessage().getMessage(map.getMapName(), "game.mapSelected"));

			//Bukkit.broadcastMessage(ChatColor.GREEN + "The map " + ChatColor.GOLD + map.getMapName() + ChatColor.GREEN + " has been selected!");
		}
		if(preGameSeconds == 30 || preGameSeconds <= 10)
			sendStartingMessage();			
		preGameSeconds--;
		if(preGameSeconds < 0) startGame();
	}
	
	public void sendStartingMessage(){
		if(preGameSeconds == 0) return;
		if(preGameSeconds == 10 || preGameSeconds <= 5){
			for(PlayerData data : players){
				Player player = data.getPlayer();
				//player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1f);
			}
		}
		if(preGameSeconds == 1)
			//Bukkit.broadcastMessage(ChatColor.GREEN + "Starting in " + ChatColor.GOLD + preGameSeconds + ChatColor.GREEN + " second!");
			sendMessage(this.getConfigMessage().getMessage("game.lastGameStartingMessage"));

		else
			//Bukkit.broadcastMessage(ChatColor.GREEN + "Starting in " + ChatColor.GOLD + preGameSeconds + ChatColor.GREEN + " seconds!");
			sendMessage(this.getConfigMessage().getMessage(preGameSeconds, "game.gameStartingMessage"));

	}

	
	public void inGameUpdate(){
		if(inGameTime <= 0){
			inGameTime = 0;
			sendMessage(this.getConfigMessage().getMessage("game.timerEnd"));
			SpleggHandler handler = this;
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	            @Override
	            public void run() {
	            	handler.restart();          	
	            }
			}, config.getLong("game.restartTime"));
			return;
		}
		
		inGameTime--;

		if(inGameCountdown > 0){
			gameStartingTitle();
			inGameCountdown--;
		}else if(inGameCountdown == 0){
			gameStartTitle();
			for(PlayerData data : players){
				Player player = data.getPlayer();
				player.getInventory().addItem(new ItemStack(Material.IRON_SPADE));
				data.getPlayerStats().addGamePlayed();
			}
			inGameCountdown--;
		}
		
		

	}
	
	public void gameStartingTitle(){
		for(PlayerData player : players){
			String title = this.getConfigMessage().getMessage(this.inGameCountdown, "game.gameStartingTitle");
			//player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.NOTE_STICKS, 1f, 0.25f);
			player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.NOTE_PLING, 1f, 0.5f);

			TitleAPI.sendFullTitle(player.getPlayer(), 0, 25, 10, title, "");
		}
	}
	
	public void gameStartTitle(){
		for(PlayerData player : players){
			String title = this.getConfigMessage().getMessage("game.gameStartTitle");
			player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.NOTE_PLING, 1f, 1f);

			TitleAPI.sendFullTitle(player.getPlayer(), 0, 10, 10, ChatColor.GREEN + "" + ChatColor.BOLD + "Splegg!", "");
		}
	}

	
	public void startGame(){
		this.gameState = GameState.INGAME;
		this.spleggScoreboard = new InGameScoreboard(this, configMessage);
		for(PlayerData data : players){
			Player player = data.getPlayer();
			player.teleport(mid);
			spleggScoreboard.addPlayer(player);
			for(PlayerData d : players){
				if(d.getPlayer().equals(data.getPlayer())) continue;
				d.getPlayer().showPlayer(data.getPlayer());
			}
		}
	}
	
	public void playerJoin(Player player){
		player.setSaturation(100000000);
		player.setFoodLevel(20);
		player.getInventory().clear();
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		PlayerData data = PlayerData.getPlayerData(player);
		data.setSpleggHandler(this);
		if(gameState.equals(GameState.PREGAME)){
			player.setGameMode(GameMode.ADVENTURE);
			if(!players.contains(data)) players.add(data);			
			player.teleport(lobbyLocation);
			sendMessage(this.getConfigMessage().getMessage(player, "game.playerJoin"));
			spleggScoreboard.addPlayer(player);

			//Bukkit.broadcastMessage(ChatColor.GREEN + "➣ " + ChatColor.GOLD + player.getName() + ChatColor.GREEN + " has joined. " + ChatColor.GRAY + "[" + ChatColor.GOLD + players.size() + "/" + maxPlayers + ChatColor.GRAY + "]");
			mapVote.sendVotingOptions(data, this);	
		}else {
			player.setGameMode(GameMode.CREATIVE);
			player.teleport(mid);
			data.setDead(true);
			if(!players.contains(data)) players.add(data);			
		}
	}
	
	public void playerQuit(Player player){
		players.remove(PlayerData.getPlayerData(player));
		if(gameState.equals(GameState.PREGAME)){
			sendMessage(this.getConfigMessage().getMessage(player, "game.playerQuit"));
		}else if(!PlayerData.getPlayerData(player).isDead()){
			PlayerData.getPlayerData(player).setDead(true);
		}
		PlayerData.getPlayerData().remove(PlayerData.getPlayerData(player));
	}
	
	public void playerVote(Player player, int number){
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
	
	public String getMinutesLeft(){
		return "" + (int) (1.0 * inGameTime / (60));
	}
	
	public String getSecondsLeft(){
		String seconds = "" + (int) (1.0 * inGameTime) % 60;
		if(seconds.length() == 1) seconds = "0" + seconds;
		return seconds;
	}
	
	boolean won = false;
	public void win(PlayerData player){
		if(won) return;
		won = true;
		gameState = GameState.OVER;
		player.getPlayerStats().addWin();
		String title = this.getConfigMessage().getMessage(player.getPlayer(), "game.playerWinTitle");
		player.getPlayer().setGameMode(GameMode.CREATIVE);
		for(PlayerData data : players){			
			TitleAPI.sendFullTitle(data.getPlayer(), 10, 120, 20, title, "");
		}
		
		for(PlayerData data : players){
			for(PlayerData d : players){
				if(!d.getPlayer().equals(data.getPlayer()))
					d.getPlayer().showPlayer(data.getPlayer());
			}
		}
		SpleggHandler handler = this;
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
            	handler.restart();          	
            }
		}, config.getLong("game.restartTime"));	
	}
	
	public void spawnFireworks(Location loc, int i){
		if(i <= 0){			
			restart();
			return;
		}
		 Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	            @Override
	            public void run() {
	            	
	            	Location rLoc = getRandomLoc(loc, 30);
	            	
	            	//Firework fireWork = (Firework) rLoc.getWorld().spawnEntity(rLoc, EntityType.FIREWORK);
	               // FireworkMeta meta = fireWork.getFireworkMeta();
	                
	                int r = rand.nextInt(256);
	                int g = rand.nextInt(256);
	                int b = rand.nextInt(256);

	                int r1 = rand.nextInt(256);
	                int g1 = rand.nextInt(256);
	                int b1 = rand.nextInt(256);
	                
	                //FireworkEffect effect = FireworkEffect.builder().with(org.bukkit.FireworkEffect.Type.BALL_LARGE).flicker(true).withColor(Color.fromRGB(r,g,b)).withColor(Color.fromRGB(r1,g1,b1)).withTrail().build();         
	                //meta.addEffect(effect);
	               // fireWork.setFireworkMeta(meta);
	            	
	                spawnFireworks(loc, i-1);
	            }
	     }, 5L);
	}
	
	public void sendMessage(String string){
		for(PlayerData data : players){
			data.getPlayer().sendMessage(string);
		}
	}
	
	
	
	Random rand = new Random();
	public Location getRandomLoc(Location loc, int radius){
		
		 int x = rand.nextInt(radius);
		 int z = rand.nextInt(radius);
		 
		 if(Math.random() >= 0.5) x = -x;
		 if(Math.random() >= 0.5) z = -z;

		 
		 return loc.clone().add(x,0,z);
		
	}
	


	public enum GameState{
		PREGAME,
		INGAME,
		OVER;
	}
	
}

