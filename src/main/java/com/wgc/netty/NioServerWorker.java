package com.wgc.netty;

import com.wgc.netty.pool.NioSelectorRunnablePool;
import com.wgc.netty.pool.Worker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * Worker实现类
 * Created by Administrator on 8/2/2018.
 */
public class NioServerWorker extends AbstractNioSelector implements Worker {

    public NioServerWorker(Executor executor, String threadName, NioSelectorRunnablePool selectorRunnablePool) {
        super(executor, threadName, selectorRunnablePool);
    }

    @Override
    protected void process(Selector selector) throws IOException {
        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty()) {
            return;
        }
        Iterator<SelectionKey> ite = keys.iterator();
        while (ite.hasNext()) {
            SelectionKey key = ite.next();
            ite.remove();//移除,防止重复处理
            SocketChannel channel = (SocketChannel) key.channel();// 得到事件发生的Socket通道

            //数据总长度
            int len = 0;
            boolean failure = true;
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //读取数据
            try {
                len = channel.read(buffer);
                failure = false;
            } catch (IOException e) {
                e.printStackTrace();
            }

            //判断连接是否断开
            if (len < 0 || failure) {
                key.cancel();
                System.out.println("客户端断开链接。。");
            } else {
                System.out.println("收到数据:" + new String(buffer.array()));

                //回写数据
                ByteBuffer outBuffer = ByteBuffer.wrap("收到\n".getBytes());
                channel.write(outBuffer);
            }

        }
    }

    /**
     * 加入一个新的Socket客户端
     *
     * @param channel
     */
    public void registerNewChannelTask(final SocketChannel channel) {
        final Selector selector = this.selector;
        registerTask(new Runnable() {
            public void run() {
                try {
                    channel.register(selector, SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected int select(Selector selector)throws IOException{
        return selector.select(500);
    }

}
