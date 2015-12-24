package org.trompgames.splegg;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BlockIterator;

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

        PlayerData data = PlayerData.getPlayerData(player);
        if (!data.canShoot()) return;

        Egg egg = (Egg) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.EGG);
        egg.setShooter(player);
        egg.setVelocity(player.getLocation().getDirection().multiply(2));
        egg.setCustomName(player.getName());

        data.shoot();

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        spleggMain.getHandler().playerJoin(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        spleggMain.getHandler().playerQuit(player);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {

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

        block.setType(Material.AIR);


    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

}