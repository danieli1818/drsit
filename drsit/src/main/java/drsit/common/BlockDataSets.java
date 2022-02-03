package drsit.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import drsit.utils.MacroUtils;
import drsit.utils.files.FileConfigurationsManager;

public class BlockDataSets {

	private String configFilename;
	private String macroFilename;
	private Map<String, Set<BlockData>> blockDataSetsMap;
	
	private static BlockDataSets instance;
	
	private static final String defaultConfigFile = "config.yml";
	
	private BlockDataSets(String configFilename, String macroFilename) {
		this.configFilename = configFilename;
		this.macroFilename = macroFilename;
		this.blockDataSetsMap = new HashMap<>();
	}
	
	public static BlockDataSets getInstance(String configFilename, String macroFilename) {
		if (instance == null) {
			instance = new BlockDataSets(configFilename, macroFilename);
		}
		return instance;
	}
	
	public static BlockDataSets getInstance(String configFilename) {
		if (instance == null) {
			instance = new BlockDataSets(configFilename, configFilename);
		}
		return instance;
	}
	
	public static BlockDataSets getInstance() {
		return getInstance(defaultConfigFile);
	}
	
	private Set<BlockData> loadBlockDataSet(String id, FileConfiguration config) {
		List<String> blockDataStrList = config.getStringList(id);
		MacroUtils.reloadInstance(this.macroFilename);
		Set<BlockData> blockDataSet = MaterialsMacrosUtils.getSetOfMaterials(blockDataStrList, MacroUtils.getInstance(this.macroFilename));
		if (blockDataSet != null) {
			this.blockDataSetsMap.put(id, blockDataSet);
		}
		return this.blockDataSetsMap.get(id);
	}
	
	public Set<BlockData> loadBlockDataSet(String id) {
		FileConfiguration config = FileConfigurationsManager.getInstance().getFileConfiguration(this.configFilename);
		return loadBlockDataSet(id, config);
	}
	
	public Set<BlockData> getBlockDataSet(String id) {
		return this.blockDataSetsMap.get(id);
	}
	
	public boolean loadBlockDataSets(Collection<String> ids) {
		boolean hasFullyLoaded = true;
		FileConfiguration config = FileConfigurationsManager.getInstance().getFileConfiguration(this.configFilename);
		for (String id : ids) {
			if (loadBlockDataSet(id, config) == null) {
				hasFullyLoaded = false;
			}
		}
		return hasFullyLoaded;
	}
	
	public boolean reloadAllBlockDataSets() {
		return loadBlockDataSets(this.blockDataSetsMap.keySet());
	}
	
	
}
