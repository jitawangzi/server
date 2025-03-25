package cn.game.core.manager;

/**
 * 管理器配置类
 */
public class ManagerConfig {
	private boolean eventNotificationEnabled = false;
    
    public ManagerConfig() {}
    
    public ManagerConfig(boolean eventNotificationEnabled) {
        this.eventNotificationEnabled = eventNotificationEnabled;
    }
    
    public boolean isEventNotificationEnabled() {
        return eventNotificationEnabled;
    }
    
    public void setEventNotificationEnabled(boolean eventNotificationEnabled) {
        this.eventNotificationEnabled = eventNotificationEnabled;
    }
}
