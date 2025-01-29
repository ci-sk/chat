package org.example.netty.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.dto.ChatMessage;
import org.example.utils.JwtUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // 管理在线用户
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    //存储客户端ID和对应的ChannelHandlerContext的映射
    private static final Map<String, ChannelHandlerContext> clients = new ConcurrentHashMap<>();

    private final JwtUtils jwtUtils;

    public WebSocketMessageHandler(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
    }
    /**
     * 当客户端连接到服务器时调用
     *
     * @param ctx 通道处理上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 获取连接信息
        System.out.println("执行顺序1");
        // 获取客户端ID
        String clientId = ctx.channel().id().asShortText();
        // 将客户端ID和对应的ChannelHandlerContext存储到映射中
        clients.put(clientId, ctx);
        // 向客户端发送其ID
        ctx.writeAndFlush(new TextWebSocketFrame("Your client ID: " + clientId));
        // 记录客户端连接信息
        log.info("Client connected: {}", clientId);
        super.channelInactive(ctx);
    }

    /**
     * 当接收到客户端发送的消息时调用
     *
     * @param ctx 通道处理上下文
     * @param frame 接收到的消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        // 获取客户端ID
        String clientId = ctx.channel().id().asShortText();
        // 解析客户端发送的JSON消息
        String jsonMessage = frame.text();

        if (!jsonMessage.contains("type")) {
            Integer uid = jwtUtils.getUid(jsonMessage);
            clients.put(String.valueOf(uid),  ctx);
            // 向客户端发送确认消息
            ctx.writeAndFlush(new  TextWebSocketFrame("Your custom client ID: " + uid));
            // 记录日志
            log.info("Client  connected with custom ID: {}", uid);
        }else{
            System.out.println("Received message: " + jsonMessage);
            ChatMessage payload = JSON.parseObject(jsonMessage, ChatMessage.class);
//        // 根据消息类型处理
            if ("private".equals(payload.getType())) {
                // 私聊消息：发送给指定客户端
                ChannelHandlerContext targetCtx = clients.get(payload.getTargetUserId());
                if (targetCtx != null) {
                    targetCtx.writeAndFlush(new TextWebSocketFrame("Private message from " + ctx.channel().id().asShortText() + ": " + payload.getContent()));
                } else {
                    ctx.writeAndFlush(new TextWebSocketFrame("Error: Target user " + payload.getTargetUserId() + " not found"));
                }
            } else if ("broadcast".equals(payload.getType())) {
                // 广播消息：发送给所有客户端
                for (ChannelHandlerContext clientCtx : clients.values()) {
                    clientCtx.writeAndFlush(new TextWebSocketFrame("Broadcast message from " + ctx.channel().id().asShortText() + ": " + payload.getContent()));
                }
            } else {
                // 未知消息类型
                ctx.writeAndFlush(new TextWebSocketFrame("Error: Unknown message type"));
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 获取客户端ID
        String clientId = ctx.channel().id().asShortText();
        // 移除clientId的映射
        clients.remove(clientId);
        // 遍历clients映射，找到与当前ChannelHandlerContext对应的自定义uid并移除
        clients.entrySet().removeIf(entry -> entry.getValue().equals(ctx));
        // 记录客户端断开连接信息
        log.info("Client disconnected: {}", clientId);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("发生异常: " + cause.getMessage());
        ctx.close();
    }
}