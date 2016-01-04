
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

import main.java.org.trompgames.splegg.PlayerData.PlayerStats;
import main.java.org.trompgames.utils.MapVote;
import main.java.org.trompgames.utils.Schematic;
import main.java.org.trompgames.utils.SpleggMap;
import main.java.org.trompgames.utils.Updateable;


public class SpleggHandler extends Updateable{

	public static int minPlayers = 2;
	public static int maxPlayers = 24;	
	
	private GameState gameState = GameState.PREGAME;
	
	private final int COUNTDOWNSECONDS;
	private int preGameSeconds;
	
	private final int INGAMECOUNTDOWN = 5;
	private int inGameCountdown = INGAMECOUNTDOWN;
	
	private final int INGAMETIME = 600 + INGAMECOUNTDOWN + 1;
	private int inGameTime = INGAMETIME;
	
	private World world;
	private Location lobbyLocation;
	private Location mid;
	private double y;
	
	private SpleggMap map;
	private MapVote mapVote;
	private FileConfiguration config;
    private ConfigMessage configMessage;
    private String configName;

    private SpleggScoreboard spleggScoreboard;
    
	private SpleggMain plugin; 
	
	ArrayList<PlayerData> players = new ArrayList<>();
	
	public static ArrayList<SpleggHandler> handlers = new ArrayList<SpleggHandler>();
	
	public void restart(){
		sendMessage(this.configMessage.getMessage("game.gameRestart", this));
		PlayerStats.saveStats();
		gameState = GameState.PREGAME;
		this.mapVote = new MapVote(config, configMessage, this);

		ArrayList<PlayerData> datas = (ArrayList<PlayerData>) players.clone();
		players.clear();
		for(int i = datas.size()-1; i >= 0; i--){
			datas.get(i).remove();
		}
		
		sendUpdate();

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
		this.spleggScoreboard = new PreGameScoreboard(this, configMessage);
		for(PlayerData data : players){
			spleggScoreboard.addPlayer(data.getPlayer());
		}
		clear();
	}
	
	
	private void sendUpdate(){
		String sentServer = config.getString("bungee.kickServer");
		//Bukkit.broadcastMessage("Sent server: " + sentServer);

		try{
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("getStats");
			out.writeUTF(sentServer);
			out.writeUTF(config.getString("bungee.serverName"));
			
			out.writeInt(getPlayers().size());
			out.writeInt(getMaxPlayers());
			out.writeUTF(getGameState().toString());
			out.writeUTF("Voting...");
			
			Bukkit.getOnlinePlayers().iterator().next().sendPluginMessage(plugin, config.getString("bungee.pluginChannel"), out.toByteArray());
		}catch(Exception e){
			Bukkit.broadcastMessage("[Splegg] error -.-");
			e.printStackTrace();
		}				
	}
	
	private void clear(){
		int radius = 100;
		for(int x = mid.getBlockX() - radius; x <= mid.getBlockX() + radius; x++){
			for(int y = 0; y <= 200; y++){
				for(int z = mid.getBlockZ() - radius; z <= mid.getBlockZ() + radius; z++){
					Location loc = new Location(world, x, y, z);
					if(!loc.getBlock().getType().equals(Material.AIR)){
						loc.getBlock().setType(Material.AIR);
					}
				}
			}
				
		}
		
		
	}
	
	protected SpleggHandler(Location lobbyLocation, Location mid, double y, String configName, FileConfiguration config, SpleggMain plugin, ConfigMessage configMessage){
		super(1);
		this.lobbyLocation = lobbyLocation;
		this.world = lobbyLocation.getWorld();
		this.mid = mid;
		this.y = y;
		this.mapVote = new MapVote(config, configMessage, this);
		this.config = config;
		this.plugin = plugin;
		this.configMessage = configMessage;
		this.spleggScoreboard = new PreGameScoreboard(this, configMessage);
		this.configName = configName;
		minPlayers = config.getInt("game.minPlayers");
		maxPlayers = config.getInt("game.maxPlayers");
		this.COUNTDOWNSECONDS = config.getInt("game.pregameTime");
		this.preGameSeconds = this.COUNTDOWNSECONDS;
		
		
		clear();

		//Schematic.loadArea(plugin, world, new File("plugins/WorldEdit/schematics/clear.schematic"), mid, true);
		handlers.add(this);
	}

	private int ticks = 0;
	@Override
	protected void update() {
		ticks++;
		if(gameState.equals(GameState.PREGAME) && ticks == 20) preGameUpdate();
		else if(gameState.equals(GameState.INGAME) && ticks == 20) inGameUpdate();
		if(gameState.equals(GameState.INGAME)){
			for(PlayerData player : players){
				if(player.isDead()){
					if(player.getPlayer().getLocation().getBlockY() <= y){
						player.getPlayer().teleport(mid);
						player.getPlayer().setFallDistance(0);
						player.getPlayer().setFlying(true);

					}
					continue;
				}
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
		player.teleport(mid);
		player.setFallDistance(0);
		player.setFlying(true);
		//Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " Died");
		sendMessage(this.getConfigMessage().getMessage(player, "game.playerDeath", this));
		data.setDead(true);
		data.getPlayerStats().addPoints(config.getInt("points.participation"));
		for(PlayerData p : players){
			p.getPlayer().hidePlayer(player);
		}
	}

	
	public void preGameUpdate(){
		if(!(players.size() >= minPlayers)){
			if(preGameSeconds != COUNTDOWNSECONDS){
				preGameSeconds = COUNTDOWNSECONDS;
				//Bukkit.broadcastMessage(ChatColor.GREEN + "Reseting countdown");
				sendMessage(this.getConfigMessage().getMessage("game.resetCountdown", this));

			}
			return;		
		}
		if(preGameSeconds == 10){
			map = mapVote.getWinner(world);
			map.loadMap(plugin, mid);
			sendMessage(this.getConfigMessage().getMessage(map.getMapName(), "game.mapSelected", this));

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
			sendMessage(this.getConfigMessage().getMessage("game.lastGameStartingMessage", this));

		else
			//Bukkit.broadcastMessage(ChatColor.GREEN + "Starting in " + ChatColor.GOLD + preGameSeconds + ChatColor.GREEN + " seconds!");
			sendMessage(this.getConfigMessage().getMessage(preGameSeconds, "game.gameStartingMessage", this));

	}

	
	public void inGameUpdate(){
		if(inGameTime <= 0){
			inGameTime = 0;
			sendMessage(this.getConfigMessage().getMessage("game.timerEnd", this));
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
			String title = this.getConfigMessage().getMessage(this.inGameCountdown, "game.gameStartingTitle", this);
			//player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.NOTE_STICKS, 1f, 0.25f);
			player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.NOTE_PLING, 1f, 0.5f);

			TitleAPI.sendFullTitle(player.getPlayer(), 0, 40, 10, title, "");
		}
	}
	
	public void gameStartTitle(){
		for(PlayerData player : players){
			String title = this.getConfigMessage().getMessage("game.gameStartTitle", this);
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
		if(players.size() + 1 >= maxPlayers){
			player.sendMessage(ChatColor.RED + "Error: Server full");
			return;
		}
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
			sendMessage(this.getConfigMessage().getMessage(player, "game.playerJoin", this));
			spleggScoreboard.addPlayer(player);

			//Bukkit.broadcastMessage(ChatColor.GREEN + "➣ " + ChatColor.GOLD + player.getName() + ChatColor.GREEN + " has joined. " + ChatColor.GRAY + "[" + ChatColor.GOLD + players.size() + "/" + maxPlayers + ChatColor.GRAY + "]");
			mapVote.sendVotingOptions(data, this);	
		}else {
			player.setGameMode(GameMode.CREATIVE);
			player.teleport(mid);
			data.setDead(true);
			if(!players.contains(data)) players.add(data);	
			for(PlayerData p : players){
				p.getPlayer().hidePlayer(player);
			}
		}
		sendUpdate();
	}
	
	public void playerQuit(Player player){
		players.remove(PlayerData.getPlayerData(player));
		sendUpdate();

		if(gameState.equals(GameState.PREGAME)){
			sendMessage(this.getConfigMessage().getMessage(player, "game.playerQuit", this));
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
	
	public static ArrayList<SpleggHandler> getSpleggHandlers(){
		return handlers;
	}
	
	public static SpleggHandler getSpleggHandler(String name){
		for(SpleggHandler handler : handlers){
			if(name.equals(handler.configName)) return handler;
		}
		return null;
	}
	
	boolean won = false;
	public void win(PlayerData data){
		if(won) return;
		won = true;
		gameState = GameState.OVER;
		data.getPlayerStats().addWin();
		String title = this.getConfigMessage().getMessage(data.getPlayer(), "game.playerWinTitle", this);
		data.getPlayer().setGameMode(GameMode.CREATIVE);
		data.getPlayerStats().addPoints(config.getInt("points.participation"));
		data.getPlayerStats().addPoints(config.getInt("points.win"));
		for(PlayerData d : players){			
			TitleAPI.sendFullTitle(d.getPlayer(), 10, 120, 20, title, "");
		}
		
		for(PlayerData dat : players){
			for(PlayerData d : players){
				if(!d.getPlayer().equals(dat.getPlayer()))
					d.getPlayer().showPlayer(dat.getPlayer());
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

