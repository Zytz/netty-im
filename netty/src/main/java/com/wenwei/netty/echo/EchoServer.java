package com.wenwei.netty.echo;
/**
 * Created by wenweizww on 2018/10/12.
 */


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * author:zhou_wenwei
 * mail:zhou_wenwei@wuxiapptec.com
 * date:2018/10/12
 * description:
 */
public class EchoServer {
    private static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    public static void main(String[] args) throws Exception{
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

        }else{
            sslCtx = null;
        }
        //配置两个线程池
        EventLoopGroup bossworkgroup = new NioEventLoopGroup(1);
        EventLoopGroup workgroup = new NioEventLoopGroup();

        //创建event handle
        EchoServerHandler echoServerHandler = new EchoServerHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossworkgroup, workgroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            if (sslCtx != null) {
                                pipeline.addLast(sslCtx.newHandler(channel.alloc()));
                            }
                            pipeline.addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE));

                            //p.addLast(new LoggingHandler(LogLevel.INFO));
                            pipeline.addLast(echoServerHandler);
                        }
                    })
                    .childOption(ChannelOption.SO_SNDBUF, 5)
                    .childOption(ChannelOption.SO_LINGER, 100)
            ;
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    System.out.println("测试网络链接成功");
                }
            }).sync();


            channelFuture.channel().closeFuture().sync();
        }finally {
            bossworkgroup.shutdownGracefully();
            workgroup.shutdownGracefully();
        }


    }
}
