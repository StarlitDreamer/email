package com.java.email.config;

import com.java.email.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import com.java.email.service.EmailLogService;
import com.java.email.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class NettyServerConfig {
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    private final EmailLogService emailLogService;
    private final UserService userService;

    @Value("${netty.port}")
    private int port;

    public NettyServerConfig(EmailLogService emailLogService, UserService userService) {
        this.emailLogService = emailLogService;
        this.userService = userService;
        startNettyServer();
    }
    @PostConstruct
    private void startNettyServer() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new EmailLogHandler(emailLogService))
                                .addLast(new QueryLogHandler(emailLogService,userService))
                                .addLast(new FailLogHandler(emailLogService,userService))
                                .addLast(new ReportHandler(emailLogService))
                                    .addLast(new ManualReportHandler(emailLogService))
                                .addLast(new QueryOneLogHandler(emailLogService,userService))
                                    .addLast(new FilterEmailTaskHandler(emailLogService,userService))
                                    .addLast(new FilterEmailHandler(emailLogService,userService));

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.bind(port).sync();
            log.info("Netty server started on port: {}", port);
        } catch (Exception e) {
            log.error("Failed to start Netty server", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        log.info("Netty server shutdown");
    }
} 