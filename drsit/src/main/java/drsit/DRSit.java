package drsit;

import org.bukkit.plugin.java.JavaPlugin;

import drsit.commands.ReloadCommand;
import drsit.commands.SitCommand;
import drsit.listeners.BlocksInteractEventsListener;
import drsit.listeners.DismountEventsListener;
import drsit.management.SittingManager;
import drsit.utils.SchedulerUtils;
import drsit.utils.files.ConfigurationsHelper;
import drsit.utils.files.FileConfigurationsManager;
import drsit.utils.messages.MessagesSender;
import drsit.utils.messages.MessagesStorage;

public class DRSit extends JavaPlugin {

	public DRSit() {
		String configFilename = "config.yml";
		String messagesFilename = "messages.yml";
		FileConfigurationsManager fcm = FileConfigurationsManager.getInstance(this);
		fcm.createConfigurationFile(configFilename);
		fcm.createConfigurationFile(messagesFilename);
		fcm.reloadAllFiles();
		SchedulerUtils.getInstance(this);
		String messagesPrefix = ConfigurationsHelper.getInstance().getString(configFilename, "messages_prefix");
		String errorMessagesPrefix = ConfigurationsHelper.getInstance().getString(configFilename, "error_messages_prefix");
		MessagesSender.getInstance(messagesPrefix, errorMessagesPrefix);
		MessagesStorage.getInstance().setConfigFilename(messagesFilename);
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		getServer().getPluginManager().registerEvents(BlocksInteractEventsListener.getInstance(), this);
		getServer().getPluginManager().registerEvents(new DismountEventsListener(), this);
		
		getCommand("sit").setExecutor(SitCommand.getInstance());
		getCommand("drsit").setExecutor(new ReloadCommand());

	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		
		SittingManager.getInstance().makeAllSittingPlayersDismount();
		
	}
	
}
