package cn.game.login.net.clientpacket.vertx.gm;

import cn.game.login.cache.entity.GmOpt;
import cn.game.login.mapper.GmOptMapper;
import cn.game.util.SpringContextLoader;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

/**
 * @ClassName GmOptListReq
 *
 * @description:
 * @author: ly
 * @create: 2024-09-20 16:59 @Version 1.0
 */
public class GmOptListReq implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        String page = context.request().getParam("page");
        String pageSize = context.request().getParam("pageSize");
        GmOptMapper mapper = SpringContextLoader.getContext().getBean(GmOptMapper.class);
        List<GmOpt> list = mapper.selectByPage(page == null ? 0 : Integer.parseInt(page), pageSize == null ? 10 : (Integer.parseInt(pageSize)*Integer.parseInt(page)));
        result.put("data", list);
        result.put("count", mapper.count());
        response.end(result.toString());
    }
}
