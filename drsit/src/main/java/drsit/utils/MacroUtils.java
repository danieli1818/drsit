package drsit.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

import drsit.utils.files.FileConfigurationsManager;

public class MacroUtils {

	private static Map<String, Map<String, MacroUtils>> macrosUtils;
	private static final String defaultMacrosID = "macro";
	
	private Map<String, List<String>> macros;
	private FileConfiguration config;
	private String macrosID;
	
	private MacroUtils(FileConfiguration config) {
		this.macros = new HashMap<>();
		this.config = config;
		this.macrosID = defaultMacrosID;
	}
	
	private MacroUtils(FileConfiguration config, String macrosID) {
		this.macros = new HashMap<>();
		this.config = config;
		this.macrosID = macrosID;
	}
	
	public static MacroUtils getInstance(String filename) {
		if (macrosUtils == null) {
			macrosUtils = new HashMap<>();
		}
		if (!macrosUtils.containsKey(filename)) {
			FileConfiguration config = FileConfigurationsManager.getInstance().getFileConfiguration(filename);
			if (config == null) {
				return null;
			}
			Map<String, MacroUtils> macrosUtilsMap = new HashMap<>();
			MacroUtils macroUtils = new MacroUtils(config);
			macrosUtilsMap.put(defaultMacrosID, macroUtils);
			macrosUtils.put(filename, macrosUtilsMap);
		} else {
			Map<String, MacroUtils> macrosUtilsMap = macrosUtils.get(filename);
			if (!macrosUtilsMap.containsKey(defaultMacrosID)) {
				FileConfiguration config = FileConfigurationsManager.getInstance().getFileConfiguration(filename);
				macrosUtilsMap.put(defaultMacrosID, new MacroUtils(config));
			}
		}
		return macrosUtils.get(filename).get(defaultMacrosID);
	}
	
	public static MacroUtils getInstance(String filename, String macrosID) {
		if (macrosUtils == null) {
			macrosUtils = new HashMap<>();
		}
		if (!macrosUtils.containsKey(filename)) {
			FileConfiguration config = FileConfigurationsManager.getInstance().getFileConfiguration(filename);
			if (config == null) {
				return null;
			}
			Map<String, MacroUtils> macrosUtilsMap = new HashMap<>();
			MacroUtils macroUtils = new MacroUtils(config, macrosID);
			macrosUtilsMap.put(macrosID, macroUtils);
			macrosUtils.put(filename, macrosUtilsMap);
		} else {
			Map<String, MacroUtils> macrosUtilsMap = macrosUtils.get(filename);
			if (!macrosUtilsMap.containsKey(macrosID)) {
				FileConfiguration config = FileConfigurationsManager.getInstance().getFileConfiguration(filename);
				macrosUtilsMap.put(macrosID, new MacroUtils(config));
			}
		}
		return macrosUtils.get(filename).get(macrosID);
	}
	
	public static boolean reloadInstance(String filename) {
		FileConfiguration config = FileConfigurationsManager.getInstance().reloadFile(filename);
		if (config == null) {
			return false;
		}
		if (macrosUtils == null) {
			macrosUtils = new HashMap<>();
		}
		Map<String, MacroUtils> macrosUtilPrevMap = macrosUtils.get(filename);
		if (macrosUtilPrevMap == null) {
			return false;
		}
		Map<String, MacroUtils> macrosUtilsNewMap = new HashMap<>();
		for (Entry<String, MacroUtils> macrosUtilEntry : macrosUtilPrevMap.entrySet()) {
			String macrosID = macrosUtilEntry.getKey();
			macrosUtilsNewMap.put(macrosID, new MacroUtils(config, macrosID));
		}
		macrosUtils.put(filename, macrosUtilsNewMap);
		return true;
	}
	
	public static MacroUtils reloadInstance(String filename, String macrosID) {
		FileConfiguration config = FileConfigurationsManager.getInstance().getFileConfiguration(filename);
		if (config == null) {
			return null;
		}
		Map<String, MacroUtils> macrosUtilsMap = macrosUtils.get(filename);
		if (macrosUtilsMap == null) {
			macrosUtilsMap = new HashMap<>();
		}
		MacroUtils macroUtils = new MacroUtils(config, macrosID);
		macrosUtilsMap.put(macrosID, macroUtils);
		macrosUtils.put(filename, macrosUtilsMap);
		return macrosUtils.get(filename).get(macrosID);
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public List<String> getMacro(String macro) {
		if (this.macros.containsKey(macro)) {
			return this.macros.get(macro);
		}
		Object macrosObject = config.get(this.macrosID);
		if (macrosObject instanceof MemorySection) {
			return getMacro(macro, (MemorySection)macrosObject);
		}
		return getMacro(macro, config.getMapList(this.macrosID));
	}
	
	public boolean hasMacro(String macro) {
		if (this.macros.containsKey(macro)) {
			return true;
		}
		Object macrosObject = config.get(this.macrosID);
		if (macrosObject instanceof MemorySection) {
			return hasMacro(macro, (MemorySection)macrosObject);
		}
		return hasMacro(macro, config.getMapList(this.macrosID));
	}
	
	public <T> List<T> getMacro(String macro, Function<String, T> mapFunction) {
		return getMacro(macro).stream().map(mapFunction).collect(Collectors.toList());
	}
	
	public <T> List<T> getMacroWithoutNull(String macro, Function<String, T> mapFunction) {
		return getMacro(macro).stream().map(mapFunction).filter((T object) -> (object != null)).collect(Collectors.toList());
	}
	
	public <T, C extends Collection<T>> C getMacro(String macro, Function<String, T> mapFunction, Collector<T, ?, C> collector) {
		return getMacro(macro).stream().map(mapFunction).collect(collector);
	}
	
	public <T, C extends Collection<T>> C getMacroWithoutNull(String macro, Function<String, T> mapFunction, Collector<T, ?, C> collector) {
		return getMacro(macro).stream().map(mapFunction).filter((T object) -> (object != null)).collect(collector);
	}
	
	public <T, C extends Collection<T>> C getMacroWithoutNull(String macro, Function<String, C> mapFunction,
			C startingPoint, BinaryOperator<C> reduceFunction) {
		return getMacro(macro).stream().map(mapFunction).filter((C object) -> (object != null)).reduce(startingPoint, reduceFunction);
	}
	
	private List<String> getMacro(String macro, MemorySection ms) {
		List<String> macroStrings = ms.getStringList(macro);
		List<String> macrosInMacroStrings = new ArrayList<>();
		for (int i = 0; i < macroStrings.size(); i++) {
			List<String> macroValues = getMacro(macroStrings.get(i), ms);
			if (!macroValues.isEmpty()) {
				macroStrings.addAll(macroValues);
				macrosInMacroStrings.add(macroStrings.get(i));
			}
		}
		for (String macroString : macrosInMacroStrings) {
			macroStrings.remove(macroString);
		}
		return macroStrings;
	}
	
	private boolean hasMacro(String macro, MemorySection ms) {
		return ms.contains(macro);
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getMacro(String macro, List<Map<?, ?>> macrosMapsList) {
		for (Map<?, ?> map : macrosMapsList) {
			if (map.containsKey(macro)) {
				Object macroValue = map.get(macro);
				if (macroValue instanceof List<?>) {
					List<?> macroValueList = (List<?>)macroValue;
					if (!macroValueList.isEmpty() && macroValueList.get(0) instanceof String) {
						List<String> macroStrings = (List<String>)macroValueList;
						List<String> macrosInMacroStrings = new ArrayList<>();
						for (int i = 0; i < macroStrings.size(); i++) {
							List<String> macroValues = getMacro(macroStrings.get(i), macrosMapsList);
							if (!macroValues.isEmpty()) {
								macroStrings.addAll(macroValues);
								macrosInMacroStrings.add(macroStrings.get(i));
							}
						}
						for (String macroString : macrosInMacroStrings) {
							macroStrings.remove(macroString);
						}
						return macroStrings;
					}
				}
				break;
			}
		}
		return new ArrayList<>();
	}
	
	private boolean hasMacro(String macro, List<Map<?, ?>> macrosMapsList) {
		for (Map<?, ?> map : macrosMapsList) {
			if (map.containsKey(macro)) {
				return true;
			}
		}
		return false;
	}
	
}
