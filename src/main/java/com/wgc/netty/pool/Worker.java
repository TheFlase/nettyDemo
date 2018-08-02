package com.wgc.netty.pool;

import java.nio.channels.SocketChannel;

/**
 * desc:Worker接口
 * Created by Administrator on 8/2/2018.
 */
public interface Worker {
    /**
     * desc:加入一个新的客户端会话
     * @param channel
     */
    public void registerNewChannelTask(SocketChannel channel);

}
