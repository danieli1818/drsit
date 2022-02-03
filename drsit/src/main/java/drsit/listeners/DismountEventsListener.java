package drsit.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.spigotmc.event.entity.EntityDismountEvent;

import drsit.management.SittingManager;
import drsit.management.SittingManager.SittingStatus;

public class DismountEventsListener implements Listener {

	@EventHandler
	public void onEntityDismountEvent(EntityDismountEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player)entity;
			SittingManager sm = SittingManager.getInstance();
			if (sm.isPlayerSitting(player.getUniqueId())) {
				sm.onPlayerDismount(player, event.getDismounted());
			}
		}
	}
	
	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		SittingManager sm = SittingManager.getInstance();
		if (sm.getSittingStatus(event.getPlayer().getUniqueId()) == SittingStatus.DISMOUNTING
				&& event.getCause() != TeleportCause.UNKNOWN) { // player sitting
			sm.onPlayerTeleportWhileDismounting(event.getPlayer(), event.getTo());
		}
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		SittingManager sm = SittingManager.getInstance();
		UUID uuid = sm.getPlayerSittingOnBlock(event.getBlock());
		if (uuid == null) {
			return;
		}
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			player.leaveVehicle();
		}
	}
	
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		for (Entity passenger : entity.getPassengers()) {
			if (passenger instanceof Player) {
				Player player = (Player)passenger;
				SittingManager sm = SittingManager.getInstance();
				if (sm.isPlayerSitting(player.getUniqueId())) {
					sm.onPlayerDismount(player, entity);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		SittingManager sm = SittingManager.getInstance();
		if (sm.isPlayerSitting(player.getUniqueId())) {
			sm.makeSittingPlayerDismount(player.getUniqueId());
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		SittingManager sm = SittingManager.getInstance();
		if (sm.isPlayerSitting(player.getUniqueId())) {
			sm.onPlayerDeath(player);
		}
	}
	
}
