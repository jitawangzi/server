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
    private  int itemFrom; // 来源 1，正常随机 2仓检 3保底
    private  int huiLiuAdd;
    private  int isDraw;  // 0 未抽 1 已抽
    private  int position;
    public DrawHeroInPool()
    {
    }
    public DrawHeroInPool(int itemId, int itemCount, int itemFrom, int position,int noHighQualityCount,boolean isHuiLiu) {
        this.itemId = itemId;
        this.itemCount = itemCount;
        this.itemWeight = 1;
        this.itemFrom = itemFrom;
        this.huiLiuAdd = 0;
        this.isDraw = 0;
        this.position = position;
       // calWeight(noHighQualityCount,isHuiLiu);
    }
    void calWeight(int noHighQualityCount,boolean isHuiLiu)
    {
        // 1 基础值
        ItemConfig heroConfig = ItemManager.instance().get(itemId);
        int baseweight= heroConfig.Quality ;
        for (int i = 0; i < GlobalConst.HeroRecruitQualityWeight.length; i++) {
            if(GlobalConst.HeroRecruitQualityWeight[i][0]== heroConfig.Quality) {
                baseweight= GlobalConst.HeroRecruitQualityWeight[i][1];
                break;
            }
        }
        int huiLiuAdd  = 0;
        if(isHuiLiu )
        {
            huiLiuAdd  = GlobalConst.HeroRecruitPlayerReflux;
        }
        int shuiwei  = noHighQualityCount*GlobalConst.HeroRecruitAttenuation;
        int shuaijian  =10000;
        if(itemFrom==2)
        {
            shuaijian= GlobalConst.HeroRecruitAttenuation;
            //仓检产出的数量 要重新设置
            for (int i = 0; i < GlobalConst.HeroRecruitChipRange.length; i++) {
                if(GlobalConst.HeroRecruitChipRange[i][0]== heroConfig.Quality) {
                    itemCount=Rnd.nextInt( GlobalConst.HeroRecruitChipRange[i][1], GlobalConst.HeroRecruitChipRange[i][1]);
                    break;
                }
            }
        }
        this.itemWeight =(int)((baseweight+ huiLiuAdd+shuiwei)*shuaijian/10000.0f);
        System.out.print(" itemWeight: " + itemWeight+" baseweight:"+ baseweight + " huiLiuAdd: " + huiLiuAdd+  " shuiwei:" + shuiwei );
    }

    public int getHuiLiuAdd() {
        return huiLiuAdd;
    }

    public void setHuiLiuAdd(int huiLiuAdd) {
        this.huiLiuAdd = huiLiuAdd;
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
