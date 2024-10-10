package cn.game.login.net.clientpacket.vertx.wechat;

import cn.game.login.net.clientpacket.vertx.wechat.combineModule.PrepayRequest;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.MalformedMessageException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.http.*;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;

import static com.wechat.pay.java.core.util.GsonUtil.toJson;
import static java.util.Objects.requireNonNull;

/**
 * @ClassName CombineJsapiService
 *
 * @description:
 * @author: ly
 * @create: 2024-09-27 14:37 @Version 1.0
 */
public class CombineJsapiService {
    private final HttpClient httpClient;
    private final HostName hostName;

    private CombineJsapiService(HttpClient httpClient, HostName hostName) {
        this.httpClient = requireNonNull(httpClient);
        this.hostName = hostName;
    }
    /** JsapiService构造器 */
    public static class Builder {

        private HttpClient httpClient;
        private HostName hostName;

        /**
         * 设置请求配置，以该配置构造默认的httpClient，若未调用httpClient()方法，则必须调用该方法
         *
         * @param config 请求配置
         * @return Builder
         */
        public CombineJsapiService.Builder config(Config config) {
            this.httpClient = new DefaultHttpClientBuilder().config(config).build();

            return this;
        }

        /**
         * 设置微信支付域名，可选，默认为api.mch.weixin.qq.com
         *
         * @param hostName 微信支付域名
         * @return Builder
         */
        public CombineJsapiService.Builder hostName(HostName hostName) {
            this.hostName = hostName;
            return this;
        }

        /**
         * 设置自定义httpClient，若未调用config()，则必须调用该方法
         *
         * @param httpClient httpClient
         * @return Builder
         */
        public CombineJsapiService.Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * 构造服务
         *
         * @return JsapiService
         */
        public CombineJsapiService build() {
            return new CombineJsapiService(httpClient, hostName);
        }
    }
    /**
     * JSAPI支付下单
     *
     * @param request 请求参数
     * @return PrepayResponse
     * @throws HttpException 发送HTTP请求失败。例如构建请求参数失败、发送请求失败、I/O错误等。包含请求信息。
     * @throws ValidationException 发送HTTP请求成功，验证微信支付返回签名失败。
     * @throws ServiceException 发送HTTP请求成功，服务返回异常。例如返回状态码小于200或大于等于300。
     * @throws MalformedMessageException 服务返回成功，content-type不为application/json、解析返回体失败。
     */
    public PrepayResponse prepay(PrepayRequest request) {
        String requestPath = "https://api.mch.weixin.qq.com/v3/combine-transactions/jsapi";
        PrepayRequest realRequest = request;
        if (this.hostName != null) {
            requestPath = requestPath.replaceFirst(HostName.API.getValue(), hostName.getValue());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader(Constant.ACCEPT, MediaType.APPLICATION_JSON.getValue());
        headers.addHeader(Constant.CONTENT_TYPE, MediaType.APPLICATION_JSON.getValue());
        HttpRequest httpRequest =
                new HttpRequest.Builder()
                        .httpMethod(HttpMethod.POST)
                        .url(requestPath)
                        .headers(headers)
                        .body(createRequestBody(realRequest))
                        .build();
        HttpResponse<PrepayResponse> httpResponse =
                httpClient.execute(httpRequest, PrepayResponse.class);
        return httpResponse.getServiceResponse();
    }

    private RequestBody createRequestBody(PrepayRequest request) {
        return new JsonRequestBody.Builder().body(toJson(request)).build();
    }

}
