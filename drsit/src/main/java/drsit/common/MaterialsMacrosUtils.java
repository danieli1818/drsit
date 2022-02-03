package drsit.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;

import drsit.utils.MacroUtils;

public class MaterialsMacrosUtils {
	
	public static Set<BlockData> parseBlockData(String blockDataStr, MacroUtils macros) {
		if (macros == null) {
			return new HashSet<>();
		}
		if (!macros.hasMacro(blockDataStr)) {
			Set<BlockData> blockDatas = new HashSet<>();
			Set<BlockData> bd = BlockData.fromString(blockDataStr);
			if (bd != null && !bd.isEmpty()) {
				if (bd.toArray(new BlockData[bd.size()])[0].getMaterial() != null) {
					for (BlockData blockData : bd) {
						blockDatas.add(blockData);
					}
				} else {
					String materialStr = BlockData.getMaterialAsString(blockDataStr);
					Set<BlockData> blockDatasFromMacroWithIDs = macros.getMacroWithoutNull(materialStr, (String value) -> getBlockData(value, true), new HashSet<BlockData>(), new BinaryOperator<Set<BlockData>>() {

						@Override
						public Set<BlockData> apply(Set<BlockData> t, Set<BlockData> u) {
							t.addAll(u);
							return t;
						}
						
					});
					Set<BlockData> blockDatasFromMacroWithoutIDs = macros.getMacroWithoutNull(materialStr, (String value) -> getBlockData(value, false), new HashSet<BlockData>(), new BinaryOperator<Set<BlockData>>() {

						@Override
						public Set<BlockData> apply(Set<BlockData> t, Set<BlockData> u) {
							t.addAll(u);
							return t;
						}
						
					});
					for (BlockData blockData : bd) {
						if (blockData.getData() != null) {
							int data = blockData.getData();
							for (BlockData blockDataFromMacro : blockDatasFromMacroWithoutIDs) {
								BlockData blockDataToAdd = new BlockData(blockDataFromMacro);
								blockDataToAdd.setData(data);
								blockDatas.add(blockDataToAdd);
							}
						} else {
							blockDatas.addAll(blockDatasFromMacroWithIDs);
						}
					}
				}
			}
			return blockDatas;
		}
		return macros.getMacroWithoutNull(blockDataStr, (String value) -> getBlockData(value, true), new HashSet<BlockData>(), new BinaryOperator<Set<BlockData>>() {

			@Override
			public Set<BlockData> apply(Set<BlockData> t, Set<BlockData> u) {
				t.addAll(u);
				return t;
			}
			
		});
	}

	public static Set<BlockData> getSetOfMaterials(List<String> materialsStrings, MacroUtils macro) {
		Set<BlockData> materials = new HashSet<>();
		for (String materialString : materialsStrings) {
			materials.addAll(parseBlockData(materialString, macro));
		}
		return materials;
	}
	
	public static Set<BlockData> getBlockData(String blockDataStr, boolean shouldKeepIDs) {
		Set<BlockData> bd = BlockData.fromString(blockDataStr);
		if (bd == null || bd.isEmpty()) {
			return null;
		}
		BlockData firstBlockData = bd.toArray(new BlockData[bd.size()])[0];
		if (firstBlockData.getMaterial() == null) {
			return null;
		}
		if (shouldKeepIDs) {
			return bd;
		}
		bd.clear();
		bd.add(firstBlockData);
		return bd;
	}
	
}
