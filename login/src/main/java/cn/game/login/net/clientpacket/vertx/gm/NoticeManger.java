package cn.game.login.net.clientpacket.vertx.gm;

import cn.game.core.net.vertx.VxHolder;
import cn.game.login.cache.entity.Notice;
import cn.game.login.mapper.NoticeMapper;
import cn.game.login.net.handler.LoginServerHandler;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName NoticeManger
 *
 * @description:
 * @author: ly
 * @create: 2024-09-19 16:33 @Version 1.0
 */
public class NoticeManger {
  private static NoticeManger instance = new NoticeManger();
  List<Notice> noticeList = new CopyOnWriteArrayList<>();
  NoticeMapper mapper;
  private NoticeManger() {}

  public static NoticeManger getInstance() {
    return instance;
  }

  public void init() {
    mapper = SpringContextLoader.getContext().getBean(NoticeMapper.class);
    refreshNoticeList();
  }

  public void refreshNoticeList() {
    List<Notice> list = mapper.selectAll();
    if (list != null && !list.isEmpty()) {
      long now = System.currentTimeMillis();
      list.removeIf(notice -> notice.getShowEndTimer().getTime() < now);
      noticeList.clear();
      noticeList.addAll(list);
      sortNoticeList();
    }
  }

  public boolean addNotice(
      int id, String tab, String title, String text, long showStartTimer, long showEndTimer,int orderNum) {

    Notice notice = null;
    if (id > 0) {
      for (Notice notice1 : noticeList) {
        if (notice1.getId() == id) {
          notice = notice1;
          break;
        }
      }
      if (notice == null) {
        return false;
      }
      notice.setId(id);
    } else {
      notice = new Notice();
    }

    notice.setTab(tab);
    notice.setTitle(title);
    notice.setText(text);
    notice.setCreateTime(new Date());
    notice.setOrdernum(orderNum);
    notice.setShowStartTimer(new java.util.Date(showStartTimer));
    notice.setShowEndTimer(new java.util.Date(showEndTimer));
    if (id > 0) {
      mapper.updateByPrimaryKey(notice);
      sortNoticeList();
      // RPC 通知其他 login 节点 从新加载
      VxHolder.requestRemoteServer(ServerType.Login, ServerMsg.LoginUpdateGmInfoRequest_7d000076.newBuilder().setType(LoginServerHandler.UPDATE_NOTICE).build());
    } else {
      mapper.insert(notice);
      refreshNoticeList();
      // RPC 通知其他 login 节点 从新加载
      VxHolder.requestRemoteServer(ServerType.Login, ServerMsg.LoginUpdateGmInfoRequest_7d000076.newBuilder().setType(LoginServerHandler.UPDATE_NOTICE).build());
    }
    return true;
  }

  private void sortNoticeList() {
    noticeList.sort((o1, o2) -> o1.getOrdernum() - o2.getOrdernum());
  }

  public List<Notice> getNoticeList() {
    return noticeList;
  }

  public boolean delNotice(int id) {
    for (Notice notice : noticeList) {
      if (notice.getId() == id) {
        noticeList.remove(notice);
        mapper.deleteByPrimaryKey(notice.getId());
        //  RPC 通知其他 login 节点
        VxHolder.requestRemoteServer(ServerType.Login, ServerMsg.LoginUpdateGmInfoRequest_7d000076.newBuilder().setType(LoginServerHandler.UPDATE_NOTICE).build());
        return true;
      }
    }
    return false;
  }
}
