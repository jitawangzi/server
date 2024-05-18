package cn.game.games.net.game.module.item;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.award.RewardItem;
import cn.game.protocol.generated.enume.ResourceEnum;
import cn.game.util.IndexedEnum;
import cn.game.util.IndexedEnumUtil;

public enum ItemUse implements IndexedEnum {
    EXP(2) {
        @Override
        public List<RewardItem> use(Player player, int id, int num, int param) {
//            PlayerHelper.addExp(player, num * param);
            List<RewardItem> rewardItems = new ArrayList<>();
            RewardItem rewardItem = new RewardItem();
            rewardItem.setId(ResourceEnum.Exp.getId());
            rewardItem.setCount(num * param);
            rewardItems.add(rewardItem);
            return rewardItems;
        }
    }
    ;
    private final int index;

    private ItemUse(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public abstract List<RewardItem> use(Player player, int id, int num, int param);

    public static final List<ItemUse> lists = IndexedEnumUtil.toIndexes(ItemUse.values());

    public static ItemUse valueOf(int index) {
        return IndexedEnumUtil.valueOf(lists, index);
    }

}
