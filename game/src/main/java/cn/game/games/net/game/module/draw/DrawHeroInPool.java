package cn.game.games.net.game.module.draw;

import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.util.Rnd;

public class DrawHeroInPool {
    private  int itemId;
    private  int itemCount;
    private  int itemWeight;
    private  int isDraw;  // 0 未抽 1 已抽
    private  int position;
    public  transient int quality;
    @Deprecated
    private  int itemFrom;
    @Deprecated
    private   int huiLiuAdd;
    public DrawHeroInPool()
    {
    }
    public DrawHeroInPool(int itemId, int itemCount, int position,int noHighQualityCount) {
        this.itemId = itemId;
        this.itemCount = itemCount;
        this.itemWeight = 1;
        this.isDraw = 0;
        this.position = position;
        // 1 基础值
        ItemConfig heroConfig = ItemManager.instance().get(itemId);
        int baseweight= heroConfig.Quality ;
        for (int i = 0; i < GlobalConst.HeroRecruitQualityWeight.length; i++) {
            if(GlobalConst.HeroRecruitQualityWeight[i][0]== heroConfig.Quality) {
                baseweight= GlobalConst.HeroRecruitQualityWeight[i][1];
                break;
            }
        }int shuiwei  =0;
        if(heroConfig.Quality>=GlobalConst.HeroRecruitQualityReflux) {
            if(noHighQualityCount>GlobalConst.HeroRecruitWaterCount)
            {
                shuiwei  = (noHighQualityCount-GlobalConst.HeroRecruitWaterCount)*GlobalConst.HeroRecruitWaterLevel;
            }
        }
        this.itemWeight =(  baseweight+ +shuiwei);
        quality = heroConfig.Quality;
//        System.out.println("抽卡日志--------------------当前物品随机概率-itemId=" +itemId +" itemWeight=" +itemWeight+" shuiwei:" + shuiwei);
    }

    public int getItemWeight() {
        return itemWeight;
    }

    public void setItemWeight(int itemWeight) {
        this.itemWeight = itemWeight;
    }

    public int getItemId() {
        return itemId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getIsDraw() {
        return isDraw;
    }

    public void setIsDraw(int isDraw) {
        this.isDraw = isDraw;
    }

    public int getPosition() {
        return position;
    }
}
