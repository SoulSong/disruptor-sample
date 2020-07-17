package com.shf.disruptor.example;

import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkProcessor;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * description :
 * 实现动态添加移除消费者，实现消费者的动态伸缩。
 *
 * @author songhaifeng
 * @date 2020/4/23 1:54
 */
public class DynamicAddWorkHandler {

    private static class MessageProduce implements Runnable {
        Disruptor<StubEvent> disruptor;
        int start;
        int over;

        MessageProduce(Disruptor<StubEvent> disruptor, int start, int over) {
            this.disruptor = disruptor;
            this.start = start;
            this.over = over;
        }

        @Override
        public void run() {
            for (int i = start; i < over + start; i++) {
                RingBuffer<StubEvent> ringBuffer = disruptor.getRingBuffer();
                long sequence = ringBuffer.next();
                try {
                    StubEvent event = ringBuffer.get(sequence);
                    event.setTestString("msg => " + i);
                } finally {
                    ringBuffer.publish(sequence);
                }
            }
        }
    }

    private static class DynamicHandler implements WorkHandler<StubEvent>, LifecycleAware {
        private final CountDownLatch shutdownLatch = new CountDownLatch(1);
        private String name;

        public DynamicHandler(String name) {
            this.name = name;
        }

        @Override
        public void onStart() {

        }

        @Override
        public void onShutdown() {
            shutdownLatch.countDown();
        }

        // 安全退出
        void awaitShutdown() throws InterruptedException {
            shutdownLatch.await();
            System.out.println(String.format("Current handler %s safe shutdown.", name));
        }

        @Override
        public void onEvent(StubEvent event) throws Exception {
            System.out.println(event.getTestString() + " ,thread ==> " + Thread.currentThread().getId() + " ,handler ==> " + name);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Sequence workSequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        ExecutorService executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);

        // Build a disruptor and start it.
        Disruptor<StubEvent> disruptor = new Disruptor<>(
                StubEvent.EVENT_FACTORY, 1024, DaemonThreadFactory.INSTANCE);
        RingBuffer<StubEvent> ringBuffer = disruptor.start();

        // 构建2个处理器
        DynamicAddWorkHandler.DynamicHandler handler1 = new DynamicAddWorkHandler.DynamicHandler("handler1");
        WorkProcessor<StubEvent> processor1 =
                new WorkProcessor<>(ringBuffer, ringBuffer.newBarrier(), handler1, new FatalExceptionHandler(), workSequence);

        DynamicAddWorkHandler.DynamicHandler handler2 = new DynamicAddWorkHandler.DynamicHandler("handler2");
        WorkProcessor<StubEvent> processor2 =
                new WorkProcessor<>(ringBuffer, ringBuffer.newBarrier(), handler2, new FatalExceptionHandler(), workSequence);

        // 动态添加第一个处理器
        ringBuffer.addGatingSequences(processor1.getSequence());
        // 启动处理器
        executor.execute(processor1);

        // 动态添加第二个处理器
        ringBuffer.addGatingSequences(processor2.getSequence());
        // 启动处理器
        executor.execute(processor2);

        // 生成编码0~99的消息，将被两个消费者消费
        Thread thread1 = new Thread(new MessageProduce(disruptor, 0, 100));
        thread1.start();

        Thread.sleep(2000);
        System.out.println("stop processor2.");
        // 终止第二个处理器, processor2.halt() 方法会等待第二个处理器完成对应的消息处理
        processor2.halt();

        // 生成编码100~199的消息，仅被一个消费者消费
        Thread thread2 = new Thread(new MessageProduce(disruptor, 100, 100));
        thread2.start();

        Thread.sleep(2000);
        System.out.println("restart processor2.");
        // 再次启动处理器2
        executor.execute(processor2);
        // 生成编码200~299的消息，将被两个消费者消费
        Thread thread3 = new Thread(new MessageProduce(disruptor, 200, 100));
        thread3.start();

        // 安全退出
        handler2.awaitShutdown();
        // Remove the gating sequence from the ring buffer
        ringBuffer.removeGatingSequence(processor2.getSequence());
        Thread.sleep(10 * 1000);
    }

}
