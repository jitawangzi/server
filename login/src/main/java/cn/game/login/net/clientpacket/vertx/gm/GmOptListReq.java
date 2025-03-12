package cn.game.login.net.clientpacket.vertx.gm;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.game.login.cache.entity.GmOpt;
import cn.game.login.mapper.GmOptMapper;
import cn.game.login.net.clientpacket.vertx.BaseVertxHandler;
import cn.game.util.SpringContextLoader;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmOptListReq
 *
 * @description:
 * @author: ly
 * @create: 2024-09-20 16:59 @Version 1.0
 */
@Component
public class GmOptListReq implements BaseVertxHandler {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        int page = context.request().getParam("page") == null ? 0 : Integer.parseInt(context.request().getParam("page"));
        int pageSize =  context.request().getParam("pageSize") == null ? 10 : Integer.parseInt(context.request().getParam("pageSize"));

        GmOptMapper mapper = SpringContextLoader.getContext().getBean(GmOptMapper.class);
        List<GmOpt> list = mapper.selectByPage((page - 1)*pageSize, pageSize);
        result.put("data", list);
        result.put("count", mapper.count());
        response.end(result.toString());
    }

	@Override
	public String getPath() {
		return "/gm/optList";
	}
}
