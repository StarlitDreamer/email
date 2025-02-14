package com.java.email.config;

import com.java.email.handler.AuthHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private AuthHandler authHandler;

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        
        // HTTP编解码器
        pipeline.addLast(new HttpServerCodec());
        // HTTP消息聚合器
        pipeline.addLast(new HttpObjectAggregator(65536));
        // 添加认证处理器
        pipeline.addLast(authHandler);
        
        // 这里可以添加你的其他业务处理器
    }
} 