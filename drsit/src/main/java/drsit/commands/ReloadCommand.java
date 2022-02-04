package drsit.commands;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import drsit.listeners.BlocksInteractEventsListener;
import drsit.management.SittingManager;
import drsit.utils.messages.MessagesSender;
import drsit.utils.messages.MessagesStorage;
import drsit.utils.reloader.Reloadable;
import drsit.utils.reloader.ReloaderManager;

public class ReloadCommand implements CommandExecutor {
	
	public ReloadCommand() {
		Set<Reloadable> reloadables = new HashSet<>();
		reloadables.add(BlocksInteractEventsListener.getInstance());
		reloadables.add(SitCommand.getInstance());
		reloadables.add(SittingManager.getInstance());
		reloadables.add(MessagesStorage.getInstance());
		ReloaderManager.getInstance().registerReloadables(reloadables);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 1 || !args[0].equals("reload")) {
			MessagesSender.getInstance().sendErrorMessage(MessagesStorage.getInstance().getMessage("reload_command_doesnt_exist_message"), sender);
			return false;
		}
		ReloaderManager.getInstance().reloadAllSet();
		MessagesSender.getInstance().sendMessage(MessagesStorage.getInstance().getMessage("reload_message"), sender);
		return true;
	}
	
}
