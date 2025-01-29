package org.example.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.netty.WebSocketChannelInitializer;
import org.example.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyConfig {

    @Value("${netty.port}")
    private int port;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Channel channel;

    @Autowired
    private JwtUtils jwtUtils;

    @Bean
    public WebSocketChannelInitializer webSocketChannelInitializer() {
        return new WebSocketChannelInitializer(jwtUtils);
    }

    @PostConstruct
    public void startNettyServer() throws InterruptedException {
        // 创建一个NioEventLoopGroup实例，用于接收连接，线程数为1
        bossGroup = new NioEventLoopGroup(1);
        // 创建一个NioEventLoopGroup实例，用于处理连接，线程数为默认值
        workerGroup = new NioEventLoopGroup();
        // 创建一个ServerBootstrap实例，用于配置和启动Netty服务器
        ServerBootstrap bootstrap = new ServerBootstrap();

        // 设置事件循环组
        bootstrap.group(bossGroup, workerGroup)
                // 设置通道类型为NioServerSocketChannel
                .channel(NioServerSocketChannel.class)
                // 设置子通道处理器，使用自定义的WebSocketChannelInitializer
                .childHandler(new WebSocketChannelInitializer(jwtUtils));
        // 绑定端口并启动服务器，不阻塞主线程
        channel = bootstrap.bind(port).sync().channel();
        // 打印服务器启动信息
        System.out.println("Netty server started on port " + port);
    }

    @PreDestroy
    public void stopNettyServer() {
        // 关闭服务器通道
        if (channel != null) {
            channel.close();
        }
        // 优雅地关闭bossGroup
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        // 优雅地关闭workerGroup
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        // 打印服务器关闭信息
        System.out.println("Netty server stopped");
    }
}
