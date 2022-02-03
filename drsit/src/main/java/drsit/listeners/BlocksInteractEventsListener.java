package drsit.listeners;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import drsit.common.BlockData;
import drsit.common.BlockDataSets;
import drsit.management.SittingManager;
import drsit.utils.MacroUtils;
import drsit.utils.files.FileConfigurationsManager;
import drsit.utils.reloader.Reloadable;

public class BlocksInteractEventsListener implements Listener, Reloadable {
	
	private static final String configFilename = "config.yml";
	
	private static BlocksInteractEventsListener instance;

	private Set<Action> triggerActions;
	
	private BlocksInteractEventsListener() {
		this.triggerActions = new HashSet<>();
		loadConfig();
	}
	
	public static BlocksInteractEventsListener getInstance() {
		if (instance == null) {
			instance = new BlocksInteractEventsListener();
		}
		return instance;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (triggerActions.contains(event.getAction()) && event.getHand() == EquipmentSlot.HAND) {
			ItemStack item = event.getItem();
			if (item == null || !item.getType().isBlock()) {
				if (getWhitelistSet().contains(new BlockData(block))) {
					Player player = event.getPlayer();
					if (player.hasPermission("drsit.sit.interact")) {
						SittingManager sm = SittingManager.getInstance();
						sm.makePlayerSit(player, block);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean loadConfig() {
		FileConfiguration config = FileConfigurationsManager.getInstance().getFileConfiguration(configFilename);
		Object actionsObject = config.get("actions");
		if (actionsObject != null && actionsObject instanceof List<?>) {
			this.triggerActions.addAll(((List<String>)actionsObject).stream()
					.map((String action) -> Action.valueOf(action)).collect(Collectors.toSet()));
		}
		BlockDataSets.getInstance(configFilename).loadBlockDataSet("whitelist");
		return true;
	}
	
	private Set<BlockData> getWhitelistSet() {
		return BlockDataSets.getInstance().getBlockDataSet("whitelist");
	}

	@Override
	public void reload() {
		MacroUtils.reloadInstance(configFilename);
		loadConfig();
	}

	@Override
	public Collection<String> getReloadFilenames() {
		Set<String> filenames = new HashSet<>();
		filenames.add(configFilename);
		return filenames;
	}
	
}
