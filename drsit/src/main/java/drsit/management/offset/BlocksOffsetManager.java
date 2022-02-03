package drsit.management.offset;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import drsit.common.BlockData;
import drsit.common.MaterialsMacrosUtils;
import drsit.utils.MacroUtils;

public class BlocksOffsetManager implements OffsetManager {

	private Map<BlockData, Vector> blocksOffsets;
	private MacroUtils macros;
	
	public BlocksOffsetManager(MacroUtils macros) {
		this.blocksOffsets = new HashMap<>();
		this.macros = macros;
	}
	
	@Override
	public Vector getOffsetOfBlock(Block block) {
		return this.blocksOffsets.get(new BlockData(block));
	}
	
	@Override
	public boolean loadBlocksOffsets(Map<String, String> blocksOffsets) {
		boolean returnFlag = true;
		for (Entry<String, String> blockOffset : blocksOffsets.entrySet()) {
			Vector vector = parseVector(blockOffset.getValue());
			if (vector == null) {
				returnFlag = false;
				continue;
			}
			for (BlockData bd : MaterialsMacrosUtils.parseBlockData(blockOffset.getKey(), this.macros)) {
				this.blocksOffsets.put(bd, vector);
			}
		}
		return returnFlag;
	}
	
	@Override
	public boolean reloadBlocksOffsets(Map<String, String> blocksOffsets) {
		this.blocksOffsets.clear();
		return loadBlocksOffsets(blocksOffsets);
	}
	
	private Vector parseVector(String vectorStr) {
		String[] vectorDataStrs = vectorStr.split(",");
		if (vectorDataStrs.length == 3) {
			try {
				double x = Double.parseDouble(vectorDataStrs[0].trim());
				double y = Double.parseDouble(vectorDataStrs[1].trim());
				double z = Double.parseDouble(vectorDataStrs[2].trim());
				return new Vector(x, y, z);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	@Override
	public MacroUtils getMacro() {
		return this.macros;
	}

	@Override
	public OffsetManager setMacro(MacroUtils macro) throws NullArgumentException {
		if (macro == null) {
			throw new NullArgumentException("macro");
		}
		this.macros = macro;
		return this;
	}
	
}
