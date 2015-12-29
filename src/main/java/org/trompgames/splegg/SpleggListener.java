package main.java.org.trompgames.splegg;

import org.bukkit.Bukkit;
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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BlockIterator;

import main.java.org.trompgames.splegg.SpleggHandler.GameState;

public class SpleggListener implements Listener {

    private SpleggMain spleggMain;

    public SpleggListener(SpleggMain spleggMain) {
        this.spleggMain = spleggMain;
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
        egg.setVelocity(player.getLocation().getDirection().multiply(1.3));
        egg.setCustomName(player.getName());        
        
        data.shoot();

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
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!PlayerData.hasData(player)) return;
        SpleggHandler handler = PlayerData.getPlayerData(player).getSpleggHandler();
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
       
        if (mat.equals(Material.BEDROCK) || mat.equals(Material.SIGN) || mat.equals(Material.SIGN_POST) || mat.equals(Material.WALL_SIGN) || mat.equals(Material.BARRIER) || mat.equals(Material.SKULL))
            return;        
        
        data.getPlayerStats().addBlocksDestroyed();
        
        block.setType(Material.AIR);
        block.getLocation().getWorld().playSound(block.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 1f);

    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
    	
        //event.setCancelled(true);
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
    	if(event.getEntity() instanceof Player && PlayerData.hasData((Player) event.getEntity()) && PlayerData.getPlayerData((Player) event.getEntity()).isInGame())
    		event.setCancelled(true);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
