package drsit.commands;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import drsit.listeners.BlocksInteractEventsListener;
import drsit.utils.MessagesSender;
import drsit.utils.reloader.Reloadable;
import drsit.utils.reloader.ReloaderManager;

public class ReloadCommand implements CommandExecutor {
	
	public ReloadCommand() {
		Set<Reloadable> reloadables = new HashSet<>();
		reloadables.add(BlocksInteractEventsListener.getInstance());
		reloadables.add(SitCommand.getInstance());
		ReloaderManager.getInstance().registerReloadables(reloadables);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 1 || !args[0].equals("reload")) {
			MessagesSender.getInstance().sendErrorMessage("Command doesn't exist! Did you mean /drsit reload?", sender);
			return false;
		}
		ReloaderManager.getInstance().reloadAllSet();
		MessagesSender.getInstance().sendMessage("Reloaded configs successfully!", sender);
		return true;
	}
	
}
