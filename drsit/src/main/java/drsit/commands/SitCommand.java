package drsit.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import drsit.common.BlockData;
import drsit.common.BlockDataSets;
import drsit.management.SittingManager;
import drsit.utils.messages.MessagesSender;
import drsit.utils.messages.MessagesStorage;
import drsit.utils.reloader.Reloadable;

public class SitCommand implements CommandExecutor, Reloadable {
	
	private static SitCommand instance;
	
	private static final String configFilename = "config.yml";
	
	private SitCommand() {
		
	}
	
	public static SitCommand getInstance() {
		if (instance == null) {
			instance = new SitCommand();
		}
		return instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			MessagesSender.getInstance().sendErrorMessage(MessagesStorage.getInstance().getMessage("only_players_command_message"), sender);
			return false;
		}
		Player player = (Player)sender;
		Block block = getBlockUnderAPlayer(player);
		if (block.getType() == Material.AIR && !player.hasPermission("drsit.sit.air")) {
			MessagesSender.getInstance().sendErrorMessage(MessagesStorage.getInstance().getMessage("sitting_on_air_message"), sender);
			return false;
		}
		if (!player.hasPermission("drsit.sit.bypass") && getBlacklist().contains(new BlockData(block))) {
			MessagesSender.getInstance().sendErrorMessage(MessagesStorage.getInstance().getMessage("cant_sit_on_block_message"), sender);
			return false;
		}
		SittingManager.getInstance().makePlayerSit(player, block);
		return true;
	}
	
	private Block getBlockUnderAPlayer(Player player) {
		return player.getLocation().getBlock().getRelative(BlockFace.DOWN);
	}
	
	private Set<BlockData> getBlacklist() {
		return BlockDataSets.getInstance(configFilename).getBlockDataSet("blacklist");
	}
	
	public void reload() {}
	
	@Override
	public Collection<String> getReloadFilenames() {
		Set<String> filesToReload = new HashSet<>();
		filesToReload.add(configFilename);
		return filesToReload;
	}

}
