package cn.game.games.net.game.gm.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.module.guarantee.Guarantee;
import cn.game.games.net.game.module.guarantee.GuaranteeModule;
import cn.game.protocol.generated.enume.GuaranteeTypeEnum;
import cn.game.util.DateUtil;

@Component
public class MiscGm extends AbstractGm {

    private static final Logger log = LoggerFactory.getLogger(MiscGm.class);

    @Override
    public void init() {
        register("ssss", this::ssss);
    }

    private void ssss(Player player, String[] params) {
        int p1 = getInt(params, 1);
        int p2 = getInt(params, 2);
        
        File file = new File("D:\\test\\");
        if (!file.exists()) {
            file.mkdirs(); // 创建目录
        }
        for (int k = 0; k < p2; k++) {
            try  {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            player.getDrawModule().getHeroRecruit().getDrawHeroCountMap().clear();
            String fileName = "qualityList"+ DateUtil.currentTimeSeconds() +".txt";
            // 创建一个文件来保存数据
            File outputFile = new File(file, fileName);
            StringBuilder stringBuilder = new StringBuilder();
            GuaranteeModule guaranteeModule = player.getGuaranteeModule();
            Guarantee guarantee = guaranteeModule.get(GuaranteeTypeEnum.DrawRefresh);
            guarantee.setRound(1);
            guarantee.setStage(1);
            guarantee.setCount(0);
            guarantee.setId(1);
            for (int i = 0; i < p1; i++) {
                var hero =  player.getDrawModule().getHeroRecruit();
                hero.refresh();
                List<Integer> qualityList = new ArrayList<>();
                for(var h:hero.getDrawHeroInPoolList()) {
                    qualityList.add(h.quality);
                }
                qualityList.sort((o1, o2) -> o2-o1);
                log.info("抽卡日志 抽取次数 :{}",i);
                int end= hero.radom31test();

                qualityList.add( end);
                for (int j = 0; j < qualityList.size(); j++) {  // 修改了循环条件
                    stringBuilder.append(qualityList.get(j));
                    if(j != qualityList.size()-1) {  // 修改了条件判断
                        stringBuilder.append(":");
                    }
                }
                stringBuilder.append("\n");
                // 将字符串写入文件
                try (FileWriter writer = new FileWriter(outputFile)) {
                    writer.write(stringBuilder.toString());
                } catch (IOException e) {
                    log.error("写入文件失败", e);
                }
                player.fireAndHandleEvent(EventTypeEnum.HeroRecruit);
            }

        }
    }
}
