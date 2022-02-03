package drsit.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import drsit.management.offset.BlocksOffsetManager;
import drsit.utils.MacroUtils;
import drsit.utils.MessagesSender;
import drsit.utils.SchedulerUtils;
import drsit.utils.files.FileConfigurationsManager;

public class SittingManager {
	
	private static final String configFilename = "config.yml";

	public enum SittingStatus {
		SITTING,
		BLOCK_SITTING,
		DISMOUNTING,
		STANDING
	}
	
	private static SittingManager instance;
	
	private Map<UUID, PlayerSittingData> sittingPlayersPreviousLocations;
	
	private BlocksOffsetManager blocksOffsetManager;
	
	private SittingManager() {
		this.sittingPlayersPreviousLocations = new HashMap<UUID, PlayerSittingData>();
		FileConfiguration conf = FileConfigurationsManager.getInstance().getFileConfiguration(configFilename);
		this.blocksOffsetManager = new BlocksOffsetManager(MacroUtils.getInstance(configFilename));
		this.blocksOffsetManager.loadBlocksOffsets(getOffsetsMap(conf));
	}
	
	public static SittingManager getInstance() {
		if (instance == null) {
			instance = new SittingManager();
		}
		return instance;
	}
	
	public void reloadConfigs() {
		MacroUtils.reloadInstance(configFilename);
		FileConfiguration conf = FileConfigurationsManager.getInstance().reloadFile(configFilename);
		this.blocksOffsetManager.setMacro(MacroUtils.getInstance(configFilename));
		this.blocksOffsetManager.reloadBlocksOffsets(getOffsetsMap(conf));
	}
	
	public boolean makePlayerSit(Player player, Location location) {
		if (location.getBlock() == null) {
			return false;
		}
		if (isPlayerSitting(player.getUniqueId())) {
			MessagesSender.getInstance().sendErrorMessage("You are already sitting!", player);
			return false;
		}
		setPrevSittingLocation(player.getUniqueId(), player.getLocation());
		Location processedLocation = location.clone();
		processedLocation.setY(processedLocation.getY() - 0.75);
		ArmorStand armorStandEntity = (ArmorStand)location.getWorld().spawnEntity(processedLocation, EntityType.ARMOR_STAND);
		armorStandEntity.setVisible(false);
		armorStandEntity.setGravity(false);
		armorStandEntity.setInvulnerable(true);
		armorStandEntity.addPassenger(player);
		return true;
	}
	
	public boolean makePlayerSit(Player player, Block block) {
		Location location = block.getLocation();
		location.setX(location.getX() + 0.5);
		location.setZ(location.getZ() + 0.5);
		Vector offsetVector = this.blocksOffsetManager.getOffsetOfBlock(block);
		if (offsetVector != null) {
			location.setX(location.getX() + offsetVector.getX());
			location.setY(location.getY() + offsetVector.getY());
			location.setZ(location.getZ() + offsetVector.getZ());
		}
		if (makePlayerSit(player, location)) {
			PlayerSittingData data = getPlayerSittingData(player.getUniqueId());
			if (data == null) {
				return false;
			}
			data.setSittingBlock(block);
			data.setSitting(SittingStatus.BLOCK_SITTING);
			return true;
		}
		return false;
	}
	
	public boolean isPlayerSitting(UUID uuid) {
		PlayerSittingData data = getPlayerSittingData(uuid);
		if (data == null) {
			return false;
		}
		return data.getSittingStatus().name().contains("SITTING");
	}
	
	public boolean onPlayerDismount(Player player, Entity entityDismountedFrom) {
		if (entityDismountedFrom == null || !entityDismountedFrom.getPassengers().contains(player)) {
			if (entityDismountedFrom == null) {
				return false;
			}
			if (entityDismountedFrom.getPassengers() == null) {
				return false;
			}
			return false;
		}
		entityDismountedFrom.remove();
		removeSittingPlayer(player);
		return true;
	}
	
	private boolean removeSittingPlayer(Player player) {
		PlayerSittingData data = getPlayerSittingData(player.getUniqueId());
		if (!isPlayerSitting(player.getUniqueId())) {
			return false;
		}
		data.setSitting(SittingStatus.DISMOUNTING);
		data.addAfterDismountingRunnable(() -> {
			data.setSitting(SittingStatus.STANDING);
			player.teleport(data.getPrevLocation());
			this.sittingPlayersPreviousLocations.remove(player.getUniqueId());
		});
		data.startTask();
		return true;
	}
	
	public boolean onPlayerTeleportWhileDismounting(Player player, Location teleportLocation) {
		PlayerSittingData data = getPlayerSittingData(player.getUniqueId());
		if (data.getSittingStatus() != SittingStatus.DISMOUNTING) {
			return false;
		}
		data.addAfterDismountingRunnable(() -> {
			player.teleport(teleportLocation);
		});
		return true;
	}
	
	public Location getPrevSittingLocation(UUID uuid) {
		return this.sittingPlayersPreviousLocations.get(uuid).getPrevLocation();
	}
	
	public Location setPrevSittingLocation(UUID uuid, Location location) {
		if (location == null) {
			PlayerSittingData data = this.sittingPlayersPreviousLocations.remove(uuid);
			if (data == null) {
				return null;
			}
			return data.getPrevLocation();
		}
		PlayerSittingData data = this.sittingPlayersPreviousLocations.put(uuid, new PlayerSittingData(location));
		if (data == null) {
			return null;
		}
		return data.getPrevLocation();
	}
	
	public PlayerSittingData getPlayerSittingData(UUID uuid) {
		return this.sittingPlayersPreviousLocations.get(uuid);
	}
	
	public SittingStatus getSittingStatus(UUID uuid) {
		PlayerSittingData data = getPlayerSittingData(uuid);
		if (data == null) {
			return SittingStatus.STANDING;
		}
		return data.getSittingStatus();
	}
	
	public UUID getPlayerSittingOnBlock(Block block) {
		for (UUID uuid : getSittingPlayers()) {
			PlayerSittingData data = getPlayerSittingData(uuid);
			if (data.getSittingStatus() == SittingStatus.BLOCK_SITTING) {
				if (block.getLocation().equals(data.getSittingBlock().getLocation())) {
					return uuid;
				}
			}
		}
		return null;
	}
	
	public Set<UUID> getSittingPlayers() {
		return this.sittingPlayersPreviousLocations.keySet();
	}
	
	public void onPlayerDeath(Player player) {
		if (!isPlayerSitting(player.getUniqueId())) {
			return;
		}
		Entity chair = player.getVehicle();
		if (chair != null) {
			chair.remove();
		}
	}
	
	private Map<String, String> getOffsetsMap(FileConfiguration conf) {
		Map<String, String> offsetsMap = new HashMap<>();
		Object offsetsObject = conf.get("offsets");
		if (offsetsObject == null || !(offsetsObject instanceof MemorySection)) {
			return offsetsMap;
		}
		MemorySection ms = (MemorySection)offsetsObject;
		for (String key : ms.getKeys(false)) {
			String value = ms.getString(key);
			if (value != null) {
				offsetsMap.put(key, value);
			}
		}
		return offsetsMap;
		
	}
	
	public boolean makeSittingPlayerDismount(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		PlayerSittingData data = getPlayerSittingData(player.getUniqueId());
		if (!isPlayerSitting(uuid)) {
			return false;
		}
		Entity entityDismountedFrom = player.getVehicle();
		if (entityDismountedFrom != null) {
			entityDismountedFrom.remove();
		}
		data.setSitting(SittingStatus.STANDING);
		data.cancelTask();
		player.teleport(data.getPrevLocation());
		this.sittingPlayersPreviousLocations.remove(player.getUniqueId());
		return true;
	}
	
	public void makeAllSittingPlayersDismount() {
		for (UUID uuid : this.sittingPlayersPreviousLocations.keySet()) {
			makeSittingPlayerDismount(uuid);
		}
	}
	
	private class PlayerSittingData {
		
		private SittingStatus sittingStatus;
		private Location prevLocation;
		private int taskID;
		private List<Runnable> afterDismountingRunnables;
		private Block sittingBlock;
		
		public PlayerSittingData(Location prevLocation) {
			this.sittingStatus = SittingStatus.SITTING;
			this.prevLocation = prevLocation;
			this.afterDismountingRunnables = new ArrayList<>();
			this.sittingBlock = null;
		}
		
		public Location getPrevLocation() {
			return prevLocation;
		}
		
		@SuppressWarnings("unused")
		public int getTaskID() {
			return taskID;
		}
		
		@SuppressWarnings("unused")
		public List<Runnable> getAfterDismountingRunnables() {
			return afterDismountingRunnables;
		}
		
		@SuppressWarnings("unused")
		public void setPrevLocation(Location prevLocation) {
			this.prevLocation = prevLocation;
		}
		
		@SuppressWarnings("unused")
		public void setTaskID(int taskID) {
			this.taskID = taskID;
		}
		
		@SuppressWarnings("unused")
		public void clearAfterDismountingRunnables() {
			this.afterDismountingRunnables.clear();
		}
		
		public void addAfterDismountingRunnable(Runnable runnable) {
			this.afterDismountingRunnables.add(runnable);
		}
		
		@SuppressWarnings("unused")
		public void removeAfterDismountRunnable(Runnable runnable) {
			this.afterDismountingRunnables.remove(runnable);
		}
		
		public SittingStatus getSittingStatus() {
			return this.sittingStatus;
		}
		
		public void setSitting(SittingStatus isSitting) {
			this.sittingStatus = isSitting;
		}
		
		public void startTask() {
			this.taskID = SchedulerUtils.getInstance().scheduleAsyncTask(1, () -> {
				for (Runnable runnable : this.afterDismountingRunnables) {
					runnable.run();
				}
			});
		}
		
		public boolean cancelTask() {
			return SchedulerUtils.getInstance().cancelTask(this.taskID);
		}
		
		public Block getSittingBlock() {
			return sittingBlock;
		}
		
		public void setSittingBlock(Block sittingBlock) {
			this.sittingBlock = sittingBlock;
		}
		
	}
	
}
