package com.java.email.config;

import com.java.email.common.Redis.RedisService;
import com.java.email.handler.*;
import com.java.email.service.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class NettyServerConfig {
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    private final EmailLogService emailLogService;
    private final UserService userService;
    private final EmailRecipientService emailRecipientService;
    private final EmailManageService emailManageService;

    @Value("${netty.port}")
    private int port;

    @Autowired
    private RedisService redisService;

    public NettyServerConfig(EmailLogService emailLogService, UserService userService, EmailRecipientService emailRecipientService, EmailManageService emailManageService) {
        this.emailLogService = emailLogService;
        this.userService = userService;
        this.emailRecipientService = emailRecipientService;

        this.emailManageService = emailManageService;
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
                                    .addLast(new TokenCheckHandler(redisService))
                                //.addLast(new EmailLogHandler(emailLogService))
                                //.addLast(new QueryLogHandler(emailLogService,userService))
                               // .addLast(new FailLogHandler(emailLogService,userService))
                                .addLast(new ReportHandler(emailLogService,userService))
                                    .addLast(new ManualReportHandler(emailLogService,userService))
                               // .addLast(new QueryOneLogHandler(emailLogService,userService))
                                    .addLast(new FilterEmailTaskHandler(emailLogService,userService,emailManageService))
                                    .addLast(new FilterEmailHandler(emailLogService,userService,emailRecipientService))
                                    ;

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