package drsit.management.offset;

import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import drsit.utils.MacroUtils;

public interface OffsetManager {

	public Vector getOffsetOfBlock(Block block);
	
	public boolean loadBlocksOffsets(Map<String, String> blocksOffsets);
	
	public boolean reloadBlocksOffsets(Map<String, String> blocksOffsets);
	
	public MacroUtils getMacro();
	
	public OffsetManager setMacro(MacroUtils macro);
	
}
