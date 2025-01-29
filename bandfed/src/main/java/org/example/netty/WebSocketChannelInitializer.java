package org.example.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.example.netty.handler.WebSocketMessageHandler;
import org.example.utils.JwtUtils;

public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final JwtUtils jwtUtils;

    public WebSocketChannelInitializer(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        // HTTP 编解码
        pipeline.addLast(new HttpServerCodec());
        // 添加HTTP对象聚合器，用于将HTTP消息的多个部分聚合成一个完整的消息
        pipeline.addLast(new HttpObjectAggregator(65536));
        // WebSocket 协议处理器
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        // 业务处理器
        pipeline.addLast(new WebSocketMessageHandler(jwtUtils));
    }
}
