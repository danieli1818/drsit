package drsit.common;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockData {
	
	private Material material;
	private Integer data;
	
	public BlockData(Material material, Integer data) {
		this.material = material;
		this.data = data;
	}
	
	public BlockData(Material material) {
		this(material, null);
	}
	
	@SuppressWarnings("deprecation")
	public BlockData(Block block) {
		this(block.getType(), Integer.valueOf(block.getData()));
	}
	
	public BlockData(BlockData blockData) {
		this(blockData.material, blockData.data);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((material == null) ? 0 : material.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockData other = (BlockData) obj;
		if (material != other.material)
			return false;
		if (data == null || other.data == null) {
			return true;
		}
		return data == other.data;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public Integer getData() {
		return data;
	}
	
	public void setData(Integer data) {
		this.data = data;
	}
	
	public static Set<BlockData> fromString(String blockDataStr) {
		Set<BlockData> blockDatas = new HashSet<>();
		String[] materialAndData = blockDataStr.split("\\|", 2);
		Material material = null;
		Set<Integer> datas = new HashSet<>();
		String materialStr = materialAndData[0];
		if (materialAndData.length == 2) {
			datas.addAll(parseDatas(materialAndData[1]));
		} else {
			datas.add(null);
		}
		if (doesMaterialExist(materialStr)) {
			material = Material.valueOf(materialStr);
		}
		for (Integer data : datas) {
			blockDatas.add(new BlockData(material, data));
		}
		return blockDatas;
	}
	
	private static Set<Integer> parseDatas(String datas) {
		Set<Integer> datasSet = new HashSet<>();
		if (datas == null) {
			return datasSet;
		}
		String[] datasSplittedByComma = datas.split(",");
		for (String dataStr : datasSplittedByComma) {
			datasSet.addAll(parseData(dataStr));
		}
		return datasSet;
	}
	
	private static Set<Integer> parseData(String dataStr) {
		Set<Integer> dataSet = new HashSet<>();
		try {
			int data = Integer.parseInt(dataStr);
			dataSet.add(data);
			return dataSet;
		} catch (NumberFormatException e) {
			String[] splittedData = dataStr.split("-");
			if (splittedData.length != 2) {
				return dataSet;
			}
			try {
				int dataMin = Integer.parseInt(splittedData[0]);
				int dataMax = Integer.parseInt(splittedData[1]);
				if (dataMin > dataMax) { // swap
					dataMin += dataMax;
					dataMax = dataMin - dataMax;
					dataMin -= dataMax;
				}
				for (int i = dataMin; i <= dataMax; i++) {
					dataSet.add(i);
				}
			} catch (NumberFormatException eInner) {
				
			}
		}
		return dataSet;
	}
	
	private static boolean doesMaterialExist(String materialStr) {
		for (Material material : Material.values()) {
			if (material.toString().equals(materialStr)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return this.material.toString() + "|" + this.data;
	}
	
	public static String getMaterialAsString(String blockDataStr) {
		if (blockDataStr == null) {
			return null;
		}
		String[] materialAndData = blockDataStr.split("\\|", 2);
		return materialAndData[0];
	}
	
}
