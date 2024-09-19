package cn.game.login.net.clientpacket.vertx.gm;

import cn.game.games.cache.entity.Notice;
import cn.game.games.net.data.mapper.NoticeMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;

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

  private NoticeManger() {}

  public static NoticeManger getInstance() {
    return instance;
  }

  public void init() {
    refreshNoticeList();
  }

  private void refreshNoticeList() {
    DAO.execute(NoticeMapper.class, MapperConstant.selectAll)
        .onSuccess(
            result -> {
              List<Notice> list = (List<Notice>) result;
              if (list != null && !list.isEmpty()) {
                long now = System.currentTimeMillis();
                list.removeIf(notice -> notice.getShowEndTimer().getTime() < now);
                noticeList.clear();
                noticeList.addAll(list);
                sortNoticeList();
              }
            })
        .onFailure(
            throwable -> {
              throwable.printStackTrace();
            });
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
    } else {
      notice = new Notice();
    }

    notice.setId(id);
    notice.setTab(tab);
    notice.setTitle(title);
    notice.setText(text);
    notice.setOrdernum(orderNum);
    notice.setShowStartTimer(new java.util.Date(showStartTimer));
    notice.setShowEndTimer(new java.util.Date(showEndTimer));
    final Notice finalNotice = notice;
    if (notice.getId() > 0) {
      DAO.update(notice).onSuccess(result -> {;
        sortNoticeList();

        //TODO RPC 通知其他 login 节点 从新加载
      });
    } else {
      DAO.insert(notice)
          .onSuccess(
              result -> {
                noticeList.add(finalNotice);
                sortNoticeList();
                //TODO RPC 通知其他 login 节点 从新加载
              })
          .onFailure(
              throwable -> {
                throwable.printStackTrace();
              });
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
        DAO.delete(notice).onSuccess(result -> {;
          //TODO  RPC 通知其他 login 节点
        }).onFailure(throwable -> {
          throwable.printStackTrace();
        });
        return true;
      }
    }
    return false;
  }
}
