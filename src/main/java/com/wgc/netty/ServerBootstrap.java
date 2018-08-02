package com.wgc.netty;

import com.wgc.netty.pool.Boss;
import com.wgc.netty.pool.NioSelectorRunnablePool;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by Administrator on 8/2/2018.
 */
public class ServerBootstrap {
    private NioSelectorRunnablePool selectorRunnablePool;

    public ServerBootstrap(NioSelectorRunnablePool selectorRunnablePool) {
        this.selectorRunnablePool = selectorRunnablePool;
    }

    public void bind(final SocketAddress localAddress){
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();//获取一个ServerSocket通道
            serverChannel.configureBlocking(false);//设置为非阻塞
            serverChannel.socket().bind(localAddress);//将该通道对应的ServerSocket绑定到port端口

            Boss nextBoss = selectorRunnablePool.nextBoss();//获取一个boss线程
            nextBoss.registerAcceptChannelTask(serverChannel);//向boss注册一个ServerSocket通道
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
