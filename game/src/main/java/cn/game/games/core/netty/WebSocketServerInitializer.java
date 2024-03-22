/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package cn.game.games.core.netty;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import cn.game.util.SpringContextLoader;
import cn.game.core.net.process.Processor;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
	private static final String WEBSOCKET_PATH = "/";

	public static final GlobalTrafficShapingHandler trafficHandler = new GlobalTrafficShapingHandler(Executors.newScheduledThreadPool(
			1), 1000);
	public static TrafficCounter trafficCounter = trafficHandler.trafficCounter();
	private static HandlerState handlerState = new HandlerState();
    private final SslContext sslCtx;

    public WebSocketServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(trafficHandler);
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new IdleStateHandler(120, 120, 120, TimeUnit.SECONDS));
//        pipeline.addLast(new WebSocketServerCompressionHandler());
		pipeline.addLast(new WebSocketEncoder());
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true,
				65536, true, 10000L));
		Processor processor = (Processor) SpringContextLoader.getContext().getBean("processor");
		pipeline.addLast(new WebSocketHandler(processor, handlerState));
    }
	public static HandlerState getHandlerState() {
		return handlerState;
	}

}
