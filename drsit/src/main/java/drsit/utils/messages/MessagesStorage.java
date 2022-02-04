package drsit.utils.messages;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import drsit.utils.files.ConfigurationsHelper;
import drsit.utils.reloader.Reloadable;

public class MessagesStorage implements Reloadable {

	private static MessagesStorage instance;
	
	private String configFilename;
	
	private Map<String, String> messagesCache;
	
	private MessagesStorage() {
		this.configFilename = "messages.yml";
		this.messagesCache = new HashMap<>();
	}
	
	public static MessagesStorage getInstance() {
		if (instance == null) {
			instance = new MessagesStorage();
		}
		return instance;
	}
	
	public MessagesStorage setConfigFilename(String filename) {
		if (configFilename != null && configFilename.equals(filename)) {
			return this;
		}
		configFilename = filename;
		clearCache();
		return this;
	}
	
	public String getMessage(String messageID) {
		if (messagesCache.containsKey(messageID)) {
			return messagesCache.get(messageID);
		}
		return loadMessage(messageID);
	}
	
	private String loadMessage(String messageID) {
		String message = ConfigurationsHelper.getInstance().getString(configFilename, messageID);
		this.messagesCache.put(messageID, message);
		return message;
	}
	
	private void clearCache() {
		this.messagesCache.clear();
	}

	@Override
	public void reload() {
		clearCache();
	}

	@Override
	public Collection<String> getReloadFilenames() {
		Set<String> filenames = new HashSet<>();
		filenames.add(configFilename);
		return filenames;
	}
	
}
