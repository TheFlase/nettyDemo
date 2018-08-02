package com.wgc.netty;

import com.wgc.netty.pool.NioSelectorRunnablePool;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * desc:抽象selector线程类
 * Created by Administrator on 8/2/2018.
 */
public abstract class AbstractNioSelector implements Runnable {

    /**
     * 线程池
     */
    private final Executor executor;

    /**
     * 选择器
     */
    protected Selector selector;

    /**
     * 选择器wakenUp状态标记
     */
    protected final AtomicBoolean wakenUp = new AtomicBoolean();

    /**
     * 任务队列
     */
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();

    /**
     * 线程名称
     */
    private String threadName;

    /**
     * 线程管理对象
     */
    protected NioSelectorRunnablePool selectorRunnablePool;

    AbstractNioSelector(Executor executor, String threadName, NioSelectorRunnablePool selectorRunnablePool) {
        this.executor = executor;
        this.threadName = threadName;
        this.selectorRunnablePool = selectorRunnablePool;
        openSelector();
    }

    /**
     * 获取selector并启动线程
     */
    private void openSelector() {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a Selector.");
        }
        executor.execute(this);
    }

    public void run() {
        Thread.currentThread().setName(this.threadName);
        while (true){
            try {
                wakenUp.set(false);
                select(selector);
                processTaskQueue();
                process(selector);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行线程队列的任务
     */
    private void processTaskQueue() {
        for(;;){
            final Runnable task = taskQueue.poll();
            if(null == task){
                break;
            }
            task.run();
        }
    }

    /**
     * 注册一个任务并且激活selector
     * @param task
     */
    protected final void registerTask(Runnable task){
        taskQueue.add(task);
        Selector selector = this.selector;
        if(selector != null){
            if(wakenUp.compareAndSet(false,true)){
                selector.wakeup();
            }
        }else {
            taskQueue.remove(task);
        }
    }

    /**
     * select抽象方法
     * @param selector
     * @return
     * @throws IOException
     */
    protected abstract int select(Selector selector)throws IOException;

    /**
     * selector业务处理
     * @param selector
     * @throws IOException
     */
    protected abstract void process(Selector selector)throws IOException;

    /**
     * 获取线程管理对象
     * @return
     */
    public NioSelectorRunnablePool getSelectorRunnablePool() {
        return selectorRunnablePool;
    }
}
