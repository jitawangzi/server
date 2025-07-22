package cn.game.games.net.game.module.develop.equip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.gem.Gem;
import cn.game.games.net.game.module.develop.gem.GemModule;
import cn.game.protocol.protobuf.BaseMsg.EquipPartShowInfo;

public class EquipPartShow {

	private int pos; // 部位类型，1-6
	private int strength = 1; // 强化等级
	private Equip equip;
	private List<Gem> gemList = new ArrayList<>(); // 
	
	public EquipPartShow() {
	}
	public static List<EquipPartShow> toEquipPartShowList(Player player) {
		List<EquipPartShow> list = new ArrayList<>();
		EquipModule equipModule = player.getModule(EquipModule.class); 
		GemModule gemModule = player.getModule(GemModule.class); 
		Map<Integer, EquipPart> equipPartMap = equipModule.getEquipPartMap(); 
		equipPartMap.forEach((k, v) -> {
			EquipPartShow  partShow = new EquipPartShow();
			partShow.setPos(k);
			partShow.setStrength(v.getStrength()) ; 
			Equip equipTemp = equipModule.get(v.getEquipUid()); 
			if (equipTemp != null) {
				partShow.setEquip(equipTemp);
			}
			Map<Long, Integer> gemPosMap = v.getGemPosMap(); 
			Iterator<Entry<Long, Integer>> iterator = gemPosMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Long, Integer> entry = iterator.next();
				Gem gem = gemModule.get(entry.getKey());
				if (gem != null) {
					partShow.getGemList().add(gem);
				}
			}
			list.add(partShow); 
			
		});
		return list; 
	}

	public EquipPartShowInfo toEquipPartShowInfo() {
		EquipPartShowInfo.Builder builder = EquipPartShowInfo.newBuilder();
		builder.setType(getPos());
		builder.setStrength(getStrength());
		if (getEquip() != null) {
			builder.setEquip(getEquip().toEquipInfo());
		}
		for (Gem gem : getGemList()) {
			builder.addGems(gem.toGemInfo());
		}
		return builder.build();
	}
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}
	public int getStrength() {
		return strength;
	}
	public void setStrength(int strength) {
		this.strength = strength;
	}
	public Equip getEquip() {
		return equip;
	}
	public void setEquip(Equip equip) {
		this.equip = equip;
	}
	public List<Gem> getGemList() {
		return gemList;
	}
	public void setGemList(List<Gem> gemList) {
		this.gemList = gemList;
	}
	
	

}
