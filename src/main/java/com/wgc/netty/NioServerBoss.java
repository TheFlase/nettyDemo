package com.wgc.netty;

import com.wgc.netty.pool.Boss;
import com.wgc.netty.pool.NioSelectorRunnablePool;
import com.wgc.netty.pool.Worker;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * Boss实现类
 * Created by Administrator on 8/2/2018.
 */
public class NioServerBoss extends AbstractNioSelector implements Boss{

    public NioServerBoss(Executor executor, String threadName, NioSelectorRunnablePool selectorRunnablePool){
        super(executor, threadName, selectorRunnablePool);
    }

    @Override
    protected void process(Selector selector) throws IOException {
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        if(selectionKeys.isEmpty()){
            return;
        }
        for(Iterator<SelectionKey> i = selectionKeys.iterator();i.hasNext();){
            SelectionKey key = i.next();
            i.remove();
            ServerSocketChannel server = (ServerSocketChannel) key.channel();

            SocketChannel socketChannel = server.accept();//新客户端
            socketChannel.configureBlocking(false);//设置为非阻塞
            Worker nextWorker = getSelectorRunnablePool().nextWorker();//获取一个worker
            nextWorker.registerNewChannelTask(socketChannel);//注册新客户端接入任务
            System.out.println("新客户端连接");
        }
    }

    public void registerAcceptChannelTask(final ServerSocketChannel serverSocketChannel){
        final Selector selector = this.selector;
        registerTask(new Runnable() {
            public void run() {
                try {
                    //注册serverChannel到selector
                    serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
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
