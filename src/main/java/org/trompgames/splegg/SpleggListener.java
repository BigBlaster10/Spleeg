package main.java.org.trompgames.splegg;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BlockIterator;

import main.java.org.trompgames.splegg.SpleggHandler.GameState;

public class SpleggListener implements Listener {

    private SpleggMain spleggMain;
    private ConfigMessage configMessage;
    
    private double eggSpeed;
    private ArrayList<Material> unbreakable = new ArrayList<>();
    
    public SpleggListener(SpleggMain spleggMain, ConfigMessage configMessage) {
        this.spleggMain = spleggMain;
        this.configMessage = configMessage;
        
        this.eggSpeed = spleggMain.getConfig().getDouble("game.eggSpeed");
        List<Integer> unbreak = (List<Integer>) spleggMain.getConfig().getList("unbreakable");
        for(int un : unbreak){
        	unbreakable.add(Material.getMaterial(un));
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){
    	Player player = event.getPlayer();
    	if(!PlayerData.hasData(player) || !PlayerData.getPlayerData(player).isInGame()) return;
    	event.setCancelled(true);
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event){
    	Player player = event.getPlayer();
    	
    	if(!PlayerData.hasData(player) || !PlayerData.getPlayerData(player).isInGame()) return;
    	String s = configMessage.getMessage(player, event.getMessage(), 0, "game.playerChat", PlayerData.getPlayerData(player).getSpleggHandler());
    	event.setCancelled(true);
    	for(PlayerData data : PlayerData.getPlayerData(player).getSpleggHandler().getPlayers()){
    		data.getPlayer().sendMessage(s);
    	}
    	
    }
    
    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;
        if (!player.getItemInHand().getType().equals(Material.IRON_SPADE)) return;

        if(!PlayerData.hasData(player)) return;
        
        PlayerData data = PlayerData.getPlayerData(player);
        if(!data.isInGame()) return;
        if (!data.canShoot()) return;
        if(data.isDead()) return;
        
        SpleggHandler handler = data.getSpleggHandler();
        
        if(!handler.getGameState().equals(GameState.INGAME)) return;
        Egg egg = (Egg) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.EGG);
        egg.setShooter(player);
        egg.setVelocity(player.getLocation().getDirection().multiply(eggSpeed));
        egg.setCustomName(player.getName());        
        data.shoot(spleggMain.getConfig());
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        //event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(!PlayerData.hasData(player)) return;

    	event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if(!PlayerData.hasData(player)) return;

    	event.setCancelled(true);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
       
        if(!spleggMain.isBungeeCord()) return;
        
        SpleggHandler handler = spleggMain.getBungeeSpleggHandler();
        handler.playerJoin(player);
        event.setJoinMessage("");
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(!PlayerData.hasData(player)) return;
        SpleggHandler handler = PlayerData.getPlayerData(player).getSpleggHandler();
       	handler.playerQuit(player);
        event.setQuitMessage("");
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {

    	Entity entity = event.getEntity();
    	if(entity.getCustomName() == null) return;
        if(Bukkit.getPlayer(entity.getCustomName()) == null) return;
        Player player = Bukkit.getPlayer(entity.getCustomName());
        PlayerData data = PlayerData.getPlayerData(player);
        if(!data.isInGame()) return;
    	
        BlockIterator iterator = new BlockIterator(event.getEntity().getWorld(), event.getEntity().getLocation().toVector(), event.getEntity().getVelocity().normalize(), 0.0D, 4);
        Block block = null;
        while (iterator.hasNext()) {
            block = iterator.next();
            if (block.getType() != Material.AIR) {
                break;
            }
        }
        if (block == null) return;
        Material mat = block.getType();
       
        if(unbreakable.contains(mat)) return;      
        
        data.getPlayerStats().addBlocksDestroyed();
        
        block.setType(Material.AIR);
        block.getLocation().getWorld().playSound(block.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 1f);

        
        if(mat.equals(Material.TNT)){
        	
        	createExplosion(block.getLocation());
        	
        	
        	
        }
        
    }
    
    
    public void createExplosion(Location loc){
    	
    	Bukkit.getScheduler().scheduleSyncDelayedTask(spleggMain, new Runnable(){

			@Override
			public void run() {
				loc.getBlock().setType(Material.AIR);
		    	loc.getWorld().createExplosion(loc, 0);
		    	

		    	for(int x = loc.getBlockX() - 1; x <= loc.getBlockX() + 1; x++){
		        	for(int y = loc.getBlockY() - 1; y <= loc.getBlockY() + 1; y++){
		            	for(int z = loc.getBlockZ() - 1; z <= loc.getBlockZ() + 1; z++){
		            		Location newLoc = new Location(loc.getWorld(), x, y, z);
		            		
		            		newLoc.getWorld().spigot().playEffect(loc, Effect.CLOUD, 0, 0, 0, 0, 0, .25f, 3, 100);
		            		newLoc.getWorld().spigot().playEffect(loc, Effect.EXPLOSION, 0, 0, 0, 0, 0, .25f, 5, 100);
		            		
		            		
		            		if(newLoc.getBlock().getType().equals(Material.TNT)){
		            			createExplosion(newLoc);
		            			continue;
		            		}
		            		newLoc.getBlock().setType(Material.AIR);           		
		            	}
		        	}
		    	}
			}    		
    	}, 1L);
    	
    	
    	
    }
    

    
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
    	if(event.getSpawnReason().equals(SpawnReason.EGG))
          event.setCancelled(true);
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
    	if(event.getEntity() instanceof Player && PlayerData.hasData((Player) event.getEntity()) && PlayerData.getPlayerData((Player) event.getEntity()).isInGame())
    		event.setCancelled(true);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
