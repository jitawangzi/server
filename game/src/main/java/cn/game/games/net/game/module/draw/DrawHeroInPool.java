package cn.game.games.net.game.module.draw;

import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.manager.HeroManager;

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
    public DrawHeroInPool(int itemId, int itemCount, int itemFrom, int position) {
        this.itemId = itemId;
        this.itemCount = itemCount;
        this.itemWeight = 1;
        this.itemFrom = itemFrom;
        this.huiLiuAdd = 0;
        this.isDraw = 0;
        this.position = position;
       // calWeight();
    }
    void calWeight()
    {
        // 1 基础值
        HeroConfig heroConfig = HeroManager.instance().get(itemId);
        int baseweight= heroConfig.InitialQuality ;
        int huiLiuAdd  =0;
        float shuaijian  =1.0f;
        int shuiwei  =0;
        this.itemWeight =(int)((baseweight+ huiLiuAdd+shuiwei)*shuaijian);
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
