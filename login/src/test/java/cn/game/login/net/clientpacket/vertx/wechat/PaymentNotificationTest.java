package cn.game.login.net.clientpacket.vertx.wechat;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import cn.game.core.sdk.ChangYouPaymentNotification;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.protocol.protobuf.ServerMsg;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/** 直接调用生产回执处理链，仅替换 SDK、Game RPC 与数据库外部边界。 */
class PaymentNotificationTest {
    /** 平台结果必须等验单、Game 确认、Login 合并及平台更新，不能提前完成。 */
    @Test void receiptWaitsForAllCompletionBoundaries() {
        var f = new Fixture(); Future<Void> result = f.handler.processNotification(f.notification);
        Assertions.assertFalse(result.isComplete()); Assertions.assertEquals(0, f.ships.get());
        f.verify.complete("{\"state\":200,\"data\":\"0\"}");
        Assertions.assertFalse(result.isComplete()); Assertions.assertEquals(1, f.ships.get()); Assertions.assertEquals(0, f.writes.get());
        f.ship.complete(ServerMsg.PaymentOrderShipResponse_7d000023.newBuilder().setSuccess(true).build());
        Assertions.assertFalse(result.isComplete()); Assertions.assertEquals(1, f.writes.get()); Assertions.assertEquals(1, f.updates.get());
        f.update.complete("accepted"); Assertions.assertTrue(result.succeeded());
    }

    /** 验单失败、Game 失败和超时均不能写交付成功或通知平台完成。 */
    @Test void verificationFailureGameFailureAndTimeoutStayUnsuccessful() {
        for (int mode = 0; mode < 4; mode++) {
            var f = new Fixture(); var result = f.handler.processNotification(f.notification);
            if (mode == 0) { f.verify.fail("verification timeout"); }
            else if (mode == 1) { f.verify.complete("{\"state\":200,\"data\":\"1\"}"); }
            else {
                f.verify.complete("{\"state\":200,\"data\":\"0\"}");
                if (mode == 2) { f.ship.fail("Game timeout, result unknown"); }
                else { f.ship.complete(ServerMsg.PaymentOrderShipResponse_7d000023.newBuilder().setSuccess(false).build()); }
            }
            Assertions.assertTrue(result.failed()); Assertions.assertFalse(f.delivered);
            Assertions.assertEquals(0, f.updates.get());
        }
    }

    /** 已交付订单也不跳过玩家、账号、金额和已绑定平台单号校验。 */
    @Test void mismatchedAndHistoricalReceiptsNeverShip() {
        for (int mode = 0; mode < 6; mode++) {
            var f = new Fixture(); f.order.setIsDeliver(true);
            var receipt = f.notification.getReceipt();
            switch (mode) {
                case 0 -> receipt.setRoleId("999");
                case 1 -> receipt.setUserid("other-user");
                case 2 -> receipt.setGoodsPrice(12.01);
                case 3 -> receipt.setGoodsPrice(Double.NaN);
                case 4 -> f.order.setTransactionId("different-transaction");
                default -> f.order.setThirdOrderId("different-order");
            }
            Assertions.assertTrue(f.handler.processNotification(f.notification).failed());
            Assertions.assertEquals(0, f.ships.get()); Assertions.assertEquals(0, f.writes.get());
        }
    }

    /** 保持旧订单与 SDK 字符串 SKU 的兼容，数量及内部商品映射不凭猜测新增限制。 */
    @Test void historicalOrdersAndSdkSkuKeepExistingVerificationChain() {
        var f = new Fixture(); f.order.setDebugText(null);
        f.notification.getReceipt().setGameGoodsId("com.cy.gyxy.yuanbao1");
        f.notification.getReceipt().setGoodsNumber("2");
        var result = f.handler.processNotification(f.notification);
        Assertions.assertFalse(result.isComplete()); Assertions.assertEquals(0, f.ships.get());
        f.verify.complete("{\"state\":200,\"data\":\"0\"}");
        Assertions.assertEquals(1, f.ships.get()); Assertions.assertFalse(result.isComplete());
        f.ship.complete(ServerMsg.PaymentOrderShipResponse_7d000023.newBuilder().setSuccess(true).build());
        Assertions.assertFalse(result.isComplete()); f.update.complete("accepted");
        Assertions.assertTrue(result.succeeded());
    }

    /** 合并或平台更新抛错都不能对外完成成功；已落库结果不由失败回写回退。 */
    @Test void databaseAndPlatformFailuresPropagate() {
        for (boolean databaseFailure : List.of(false, true)) {
            var f = new Fixture(); f.failWrite = databaseFailure;
            var result = f.handler.processNotification(f.notification);
            f.verify.complete("{\"state\":200,\"data\":\"0\"}");
            f.ship.complete(ServerMsg.PaymentOrderShipResponse_7d000023.newBuilder().setSuccess(true).build());
            if (!databaseFailure) { f.update.fail("platform unavailable"); Assertions.assertTrue(f.delivered); }
            Assertions.assertTrue(result.failed()); Assertions.assertEquals(1, f.writes.get());
        }
    }

    /** 零行更新不能冒充成功，必须核对真实持久身份和交付终态。 */
    @Test void zeroRowMergeRequiresMatchingPersistedResult() {
        var f = new Fixture(); f.zeroRows = true;
        Assertions.assertThrows(IllegalStateException.class, () -> PaymentOrderState.record(f.mapper, f.order, true));
        f.order.setIsDeliver(true); f.order.setPayState((byte) 2);
        Assertions.assertDoesNotThrow(() -> PaymentOrderState.record(f.mapper, f.order, true));
    }

    /** 非数字第三方单号可达查询分支；空 SDK 身份不会传 null 给 Protobuf。 */
    @Test void gmQueriesThirdPartyIdAndHandlesNullOrBlankSdkId() {
        for (String sdk : new String[] {null, "", "  ", "sdk-alpha"}) {
            var f = new EntryFixture(); f.order.setThirdOrderId(sdk);
            f.http.id = sdk == null || sdk.isBlank() ? "91" : sdk;
            f.gm().handle(f.http.context);
            Assertions.assertEquals(1, f.ships.get()); Assertions.assertEquals(sdk == null || sdk.isBlank() ? "" : sdk, f.sent.getSdkOrderId());
            f.ship.complete(ServerMsg.PaymentOrderShipResponse_7d000023.newBuilder().setSuccess(true).build());
            Assertions.assertEquals(1, f.http.ends.get());
            Assertions.assertEquals("91".equals(f.http.id) ? 0 : 1, f.thirdQueries.get());
        }
    }

    /** GM 缺参、坏状态、坏核心字段和同步 DAO/RPC 异常均结束原 HTTP。 */
    @Test void gmRejectsInvalidOrdersAndEndsSynchronousFailures() {
        for (int mode = 0; mode < 8; mode++) {
            var f = new EntryFixture();
            switch (mode) {
                case 0 -> f.http.id = null;
                case 1 -> f.order.setPayState((byte) 9);
                case 2 -> f.order.setPlayerId(null);
                case 3 -> f.order.setPrice(null);
                case 4 -> f.order.setPrice(0);
                case 5 -> f.failRead = true;
                case 6 -> f.failShip = true;
                default -> f.order.setId(null);
            }
            f.gm().handle(f.http.context); Assertions.assertEquals(1, f.http.ends.get());
            Assertions.assertEquals(0, f.ships.get());
        }
    }

    /** 微信真实 JSON 入口须等 Game，重复、失败及 Login 异常不提前成功。 */
    @Test void wechatEntryWaitsAndPreservesSdkIdentityForGmReplay() {
        for (int mode = 0; mode < 4; mode++) {
            var f = new EntryFixture(); f.http.payload = f.http.payload.replace("\"ActualPrice\":1200", "\"ActualPrice\":900"); f.wechat().handle(f.http.context);
            Assertions.assertEquals(1, f.ships.get()); Assertions.assertEquals("", f.sent.getSdkOrderId());
            Assertions.assertEquals(0, f.http.ends.get()); Assertions.assertEquals(1, f.writes.get());
            if (mode == 0) { f.ship.fail("timeout unknown"); }
            else { f.failWrite = mode == 3; f.ship.complete(ServerMsg.PaymentOrderShipResponse_7d000023.newBuilder().setSuccess(mode != 1).build()); }
            Assertions.assertEquals(1, f.http.ends.get());
            Assertions.assertEquals(mode == 2, f.http.body.contains("\"ErrCode\":0"));
            Assertions.assertEquals(PaymentOrderState.sdkOrderId(f.order), f.sent.getSdkOrderId());
        }
    }

    /** 微信坏 JSON、缺字段、金额不符、状态和流水冲突全部在 Game 前拒绝。 */
    @Test void wechatEntryRejectsMalformedAmountsStatesAndBoundConflicts() {
        for (int mode = 0; mode < 11; mode++) {
            var f = new EntryFixture();
            switch (mode) {
                case 0 -> f.http.payload = "{";
                case 1 -> f.http.payload = "{}";
                case 2 -> f.http.payload = f.http.payload.replace("1200", "12");
                case 3 -> f.http.payload = f.http.payload.replace("91", "bad-id");
                case 4 -> f.http.payload = f.http.payload.replace("WeChatPayInfo", "MissingInfo");
                case 5 -> f.order.setPayState((byte) 9);
                case 6 -> f.order.setTransactionId("conflict");
                case 7 -> f.order.setMchOrderNo("conflict");
                case 8 -> f.failWrite = true;
                case 9 -> f.failShip = true;
                default -> f.http.payload = f.http.payload.replace("GoodsInfo", "MissingGoods");
            }
            f.wechat().handle(f.http.context);
            Assertions.assertEquals(0, f.ships.get()); Assertions.assertEquals(1, f.http.ends.get());
            Assertions.assertFalse(f.http.body.contains("\"ErrCode\":0"));
        }
    }

    /** iOS 走实际处理链但替换验签外部边界，不触发证书下载或真实支付。 */
    @Test void iosEntryUsesPrepayIdentityAndWaitsForCommit() {
        for (int mode = 0; mode < 3; mode++) {
            var f = new EntryFixture(); f.order.setThirdOrderId("prepay-91");
            f.ios(); Assertions.assertEquals(1, f.ships.get()); Assertions.assertEquals("prepay-91", f.sent.getSdkOrderId());
            Assertions.assertEquals(0, f.http.ends.get());
            if (mode == 0) { f.ship.fail("unknown timeout"); }
            else { f.ship.complete(ServerMsg.PaymentOrderShipResponse_7d000023.newBuilder().setSuccess(mode == 2).build()); }
            Assertions.assertEquals(1, f.http.ends.get()); Assertions.assertEquals(mode == 2 ? 200 : 500, f.http.status);
        }
    }

    /** iOS 先拒绝未知状态、冲突、金额和缺失字段，验签或 DAO 异常也结束响应。 */
    @Test void iosEntryRejectsInvalidAndConflictingReceiptsBeforeGame() {
        for (int mode = 0; mode < 10; mode++) {
            var f = new EntryFixture();
            switch (mode) {
                case 0 -> f.order.setPayState((byte) 9);
                case 1 -> f.order.setTransactionId("different");
                case 2 -> f.order.setMchOrderNo("different");
                case 3 -> f.order.setPrice(12);
                case 4 -> f.transaction.setPayer(null);
                case 5 -> f.transaction.setOutTradeNo("bad-id");
                case 6 -> f.transaction.setTransactionId(null);
                case 7 -> f.failRead = true;
                case 8 -> f.failWrite = true;
                default -> f.failParse = true;
            }
            f.ios(); Assertions.assertEquals(0, f.ships.get()); Assertions.assertEquals(1, f.http.ends.get());
            Assertions.assertTrue(f.http.status >= 400);
        }
    }

    /** 并发两次同流水共用身份；后到失败不能覆盖先到成功，冲突回执不会到达 Game。 */
    @Test void wechatConcurrentAndOutOfOrderResponsesRemainMonotonic() {
        var f = new EntryFixture(); var first = f.ship;
        f.wechat().handle(f.http.context); f.ship = Promise.promise();
        var secondHttp = new HttpFixture(); f.wechat().handle(secondHttp.context);
        Assertions.assertEquals(2, f.ships.get());
        f.ship.complete(ServerMsg.PaymentOrderShipResponse_7d000023.newBuilder().setSuccess(true).build());
        first.complete(ServerMsg.PaymentOrderShipResponse_7d000023.newBuilder().setSuccess(false).build());
        Assertions.assertTrue(f.delivered); Assertions.assertEquals(1, secondHttp.ends.get()); Assertions.assertEquals(1, f.http.ends.get());
        var conflict = new HttpFixture(); conflict.payload = conflict.payload.replace("txn-91", "other-txn");
        f.wechat().handle(conflict.context); Assertions.assertEquals(2, f.ships.get()); Assertions.assertEquals(1, conflict.ends.get());
    }

    /** 仅替换存储、Game RPC、GM 审计和签名边界，入口参数与响应真实执行。 */
    private static final class EntryFixture {
        final HttpFixture http = new HttpFixture();
        final PayOrder order = new PayOrder();
        final AtomicInteger ships = new AtomicInteger(), writes = new AtomicInteger(), thirdQueries = new AtomicInteger();
        Promise<ServerMsg.PaymentOrderShipResponse_7d000023> ship = Promise.promise();
        ServerMsg.PaymentOrderShipRequest_7d000022 sent;
        boolean failRead, failWrite, failShip, failParse, delivered;
        final com.wechat.pay.java.service.payments.model.Transaction transaction = com.alibaba.fastjson2.JSON.parseObject(
                "{\"outTradeNo\":\"91\",\"transactionId\":\"txn-91\",\"tradeState\":\"SUCCESS\",\"payer\":{\"openid\":\"user-91\"},\"amount\":{\"total\":1200}}",
                com.wechat.pay.java.service.payments.model.Transaction.class);
        final PayOrderMapper mapper = (PayOrderMapper) Proxy.newProxyInstance(PayOrderMapper.class.getClassLoader(), new Class<?>[]{PayOrderMapper.class}, (p,m,a) -> {
            if (failRead) { throw new IllegalStateException("read failed"); }
            if (m.getName().equals("selectByPrimaryKey")) { return order; }
            if (m.getName().equals("selectByThirdOrderId")) { thirdQueries.incrementAndGet(); return List.of(order); }
            if (m.getName().equals("mergePaymentReceipt")) {
                writes.incrementAndGet(); if (failWrite) { throw new IllegalStateException("write failed"); }
                delivered |= (boolean)a[1]; return 1;
            }
            throw new AssertionError(m.getName());
        });
        /** 独立测试订单，无业务账号。 */
        EntryFixture() {
            transaction.setOutTradeNo("91"); transaction.setTransactionId("txn-91");
            transaction.setTradeState(com.wechat.pay.java.service.payments.model.Transaction.TradeStateEnum.SUCCESS);
            var payer = new com.wechat.pay.java.service.payments.model.TransactionPayer(); payer.setOpenid("user-91"); transaction.setPayer(payer);
            var amount = new com.wechat.pay.java.service.payments.model.TransactionAmount(); amount.setTotal(1200); transaction.setAmount(amount);
            order.setId(91L); order.setPlayerId(8291L); order.setPrice(1200); order.setThirdUid("user-91"); order.setPayState((byte)1); order.setIsDeliver(false); }
        /** @param request 请求 @return 可控 Game 原提交结果 */
        Future<ServerMsg.PaymentOrderShipResponse_7d000023> send(ServerMsg.PaymentOrderShipRequest_7d000022 request) {
            if (failShip) { throw new IllegalStateException("RPC failed synchronously"); }
            sent = request; ships.incrementAndGet(); return ship.future();
        }
        /** @return 保留真实 GM handle 的测试处理器 */
        cn.game.login.net.clientpacket.vertx.gm.GmPayOrderSuccessReq gm() {
            return new cn.game.login.net.clientpacket.vertx.gm.GmPayOrderSuccessReq() {
                @Override protected PayOrderMapper paymentMapper() { return mapper; }
                @Override protected Future<ServerMsg.PaymentOrderShipResponse_7d000023> shipOrder(ServerMsg.PaymentOrderShipRequest_7d000022 request) { return send(request); }
                @Override protected void audit(String id, boolean success) {}
            };
        }
        /** @return 保留真实微信解析与 handle 的测试处理器 */
        WechatShipPush wechat() { return new WechatShipPush() {
            @Override protected boolean checkRequest(String s, String t, String n) { return true; }
            @Override protected PayOrderMapper paymentMapper() { return mapper; }
            @Override protected Future<ServerMsg.PaymentOrderShipResponse_7d000023> shipOrder(ServerMsg.PaymentOrderShipRequest_7d000022 request) { return send(request); }
        }; }
        /** 执行生产 iOS 处理入口，验签由受控外部边界替代。 */
        void ios() { IOSPayOrderProcessor.notifyPayOrder(http.context, parameter -> {
            if (failParse) { throw new IllegalArgumentException("invalid signature"); } return transaction;
        }, () -> mapper, this::send); }
    }

    /** HTTP 接口代理记录终结次数；正文仍经生产 JSON 解析。 */
    private static final class HttpFixture {
        String id = "91", body = "";
        String payload = "{\"OutTradeNo\":\"91\",\"OpenId\":\"user-91\",\"WeChatPayInfo\":{\"MchOrderNo\":\"mch-91\",\"TransactionId\":\"txn-91\"},\"GoodsInfo\":{\"OrigPrice\":1200,\"ActualPrice\":1200,\"Quantity\":2}}";
        int status = 200; final AtomicInteger ends = new AtomicInteger();
        final io.vertx.core.http.HttpServerResponse response = proxy(io.vertx.core.http.HttpServerResponse.class, (p,m,a) -> {
            if (m.getName().equals("end")) { ends.incrementAndGet(); body = a == null || a.length == 0 ? "" : a[0].toString(); return Future.succeededFuture(); }
            if (m.getName().equals("setStatusCode")) { status = (int)a[0]; }
            return p;
        });
        final io.vertx.core.http.HttpServerRequest request = proxy(io.vertx.core.http.HttpServerRequest.class, (p,m,a) -> {
            if (m.getName().equals("getParam")) { return "id".equals(a[0]) ? id : null; }
            if (m.getName().equals("getHeader")) { return "test"; } return null;
        });
        final io.vertx.ext.web.RequestBody requestBody = proxy(io.vertx.ext.web.RequestBody.class, (p,m,a) -> {
            if (m.getName().equals("asString")) { return com.alibaba.fastjson2.JSON.toJSONString(java.util.Map.of("MiniGame", java.util.Map.of("Payload", payload))); } return null;
        });
        final io.vertx.ext.web.RoutingContext context = proxy(io.vertx.ext.web.RoutingContext.class, (p,m,a) -> switch(m.getName()) {
            case "response" -> response; case "request" -> request; case "body" -> requestBody; default -> null;
        });
        /** @param type HTTP 接口类型 @param handler 调用记录器 @return 无网络接口实例 */
        private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
        }
    }

    /** 每例拥有独立订单和可控的三段异步结果，不调用真实平台。 */
    private static final class Fixture {
        final PayOrder order = new PayOrder();
        final ChangYouPaymentNotification notification = new ChangYouPaymentNotification();
        final Promise<String> verify = Promise.promise(), update = Promise.promise();
        final Promise<ServerMsg.PaymentOrderShipResponse_7d000023> ship = Promise.promise();
        final AtomicInteger ships = new AtomicInteger(), writes = new AtomicInteger(), updates = new AtomicInteger();
        boolean delivered, failWrite, zeroRows;
        final PayOrderMapper mapper = (PayOrderMapper) Proxy.newProxyInstance(PayOrderMapper.class.getClassLoader(),
                new Class<?>[] {PayOrderMapper.class}, (proxy, method, args) -> {
                    if (method.getName().equals("selectByPrimaryKey")) { return order; }
                    if (method.getName().equals("mergePaymentReceipt")) {
                        writes.incrementAndGet(); if (failWrite) { throw new IllegalStateException("commit failed"); }
                        if (zeroRows) { return 0; }
                        delivered |= (boolean) args[1]; return 1;
                    }
                    throw new AssertionError("禁止整行回写或非预期 DAO " + method.getName());
                });
        final ChangYouShipPush handler = new ChangYouShipPush() {
            @Override protected PayOrderMapper paymentMapper() { return mapper; }
            @Override protected Future<String> verifyOrder(ChangYouPaymentNotification value) { return verify.future(); }
            @Override protected Future<ServerMsg.PaymentOrderShipResponse_7d000023> shipOrder(ServerMsg.PaymentOrderShipRequest_7d000022 value) {
                Assertions.assertEquals(91, value.getUid()); Assertions.assertEquals(8291, value.getPlayerId());
                Assertions.assertEquals("sdk-91", value.getSdkOrderId()); ships.incrementAndGet(); return ship.future();
            }
            @Override protected Future<String> notifyUpdateOrder(ChangYouPaymentNotification value) { updates.incrementAndGet(); return update.future(); }
        };
        /** 构造与创单持久字段匹配的测试回执。 */
        Fixture() {
            order.setId(91L); order.setPlayerId(8291L); order.setThirdUid("user-91"); order.setPrice(1200);
            order.setPayState((byte) 1); order.setIsDeliver(false); order.setDebugText("{\"s3aItemId\":\"1001\"}");
            var receipt = new ChangYouPaymentNotification.ChangYouReceipt(); receipt.setOrderId("sdk-91");
            receipt.setRoleId("8291"); receipt.setUserid("user-91"); receipt.setGoodsPrice(12);
            receipt.setGameGoodsId("1001"); receipt.setGoodsNumber("1");
            notification.setReceipt(receipt); var push = new ChangYouPaymentNotification.ChangYouPushInfo();
            push.setGameOrderId(91); notification.setPushInfo(push);
        }
    }
}
