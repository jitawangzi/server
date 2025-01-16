package cn.game.games.net.game.module.player;

import cn.game.core.task.SchedulerService;

import java.util.concurrent.ScheduledFuture;

/**
 * @ClassName OfflineScheduleTask
 *
 * @description: 玩家离线执行的 定时 task
 * @author: ly
 * @create: 2025-01-15 16:36 @Version 1.0
 */
public class OfflineScheduleTask {
    ScheduledFuture<?> runTask;

    public OfflineScheduleTask(ScheduledFuture<?> runTask) {
        this.runTask = runTask;
    }

    private OfflineScheduleTask() {
    }

    public void cancelTask(){
        if (runTask != null){
            SchedulerService.getInstance().cancelTask(runTask);
        }
    }
}
