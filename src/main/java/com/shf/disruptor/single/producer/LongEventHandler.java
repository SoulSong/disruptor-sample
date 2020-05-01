package com.shf.disruptor.single.producer;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

import java.util.concurrent.CountDownLatch;

/**
 * description :
 * 事件处理器
 * {@link EventHandler} 默认使用的是 BatchEventProcessor，它与 EventHandler 是一一对应，并且是单线程执行。
 * {@link LifecycleAware} example : https://github.com/LMAX-Exchange/disruptor/blob/master/src/test/java/com/lmax/disruptor/example/WaitForShutdown.java
 *
 * @author songhaifeng
 * @date 2020/4/22 23:36
 */
public class LongEventHandler implements EventHandler<LongEvent>, LifecycleAware {
    private CountDownLatch shutdownLatch;

    public LongEventHandler(CountDownLatch shutdownLatch) {
        this.shutdownLatch = shutdownLatch;
    }

    /**
     * @param event      RingBuffer 已经发布的事件；
     * @param sequence   正在处理的事件的序列号；
     * @param endOfBatch 用来标识否是来自 RingBuffer 的批次中的最后一个事件；
     */
    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("Handler1(" + Thread.currentThread().getId() + "), Event: " + event + " with value: " + event.getValue());
    }

    /**
     * 线程启动时执行，发生在处理第一个事件之前
     */
    @Override
    public void onStart() {
        System.out.println("invoke onStart");
    }

    /**
     * 线程关闭时执行
     */
    @Override
    public void onShutdown() {
        System.out.println("invoke onShutdown");
        shutdownLatch.countDown();
    }
}
