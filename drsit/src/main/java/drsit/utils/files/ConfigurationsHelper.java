package drsit.utils.files;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class ConfigurationsHelper {

	private static ConfigurationsHelper instance;
	
	private FileConfigurationsManager fcm;
	
	private ConfigurationsHelper() {
		this.fcm = FileConfigurationsManager.getInstance();
	}
	
	public static ConfigurationsHelper getInstance() {
		if (instance == null) {
			instance = new ConfigurationsHelper();
		}
		return instance;
	}
	
	public String getString(String filename, String path) {
		FileConfiguration conf = fcm.getFileConfiguration(filename);
		if (conf == null) {
			return null;
		}
		return conf.getString(path);
	}
	
	public Map<String, Object> getMapFromPath(String filename, String path) {
		FileConfiguration conf = fcm.getFileConfiguration(filename);
		if (conf == null) {
			return null;
		}
		if (path == null) {
			return conf.getValues(false);
		}
		return conf.getConfigurationSection(path).getValues(false);
	}
	
	public void setMapOfSerializables(String filename, Map<String, Object> serializationMap) {
		FileConfiguration conf = fcm.getFileConfiguration(filename);
		for (String path : serializationMap.keySet()) {
			conf.set(path, serializationMap.get(path));
		}
	}
	
	public <T extends ConfigurationSerializable> boolean addConfigurationSerializablesToConfiguration(String filename, Map<String, T> serializables) {
		FileConfiguration conf = fcm.getFileConfiguration(filename);
		for (Entry<String, T> serializable : serializables.entrySet()) {
			conf.set(serializable.getKey(), serializable.getValue());
		}
		return fcm.saveFileConfigurationToFile(filename);
	}
	
	public <T extends ConfigurationSerializable> Map<String, T> getMapOfConfigurationSerializablesFromConfiguration(String name, Collection<String> paths, Class<ConfigurationSerializable> T) {
		FileConfiguration conf = fcm.getFileConfiguration(name);
		Map<String, T> configurationSerializables = new HashMap<>();
		for (String path : paths) {
			@SuppressWarnings("unchecked")
			T configurationSerializable = (T) conf.getSerializable(path, T);
			if (configurationSerializable == null) {
				continue;
			}
			configurationSerializables.put(path, configurationSerializable);
		}
		return configurationSerializables;
	}
	
}
