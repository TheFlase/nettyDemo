package com.wgc.netty.pool;

import java.nio.channels.ServerSocketChannel;

/**
 * Created by Administrator on 8/2/2018.
 */
public interface Boss {
    /**
     * desc:加入一个新的ServerSocket
     * @param serverSocketChannel
     */
    public void registerAcceptChannelTask(ServerSocketChannel serverSocketChannel);
}
