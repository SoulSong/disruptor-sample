package com.shf.disruptor.single.producer;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * description :
 * 主要验证单个生产者，多个{@link com.lmax.disruptor.EventHandler}并行独立消费
 *
 * @author songhaifeng
 * @date 2020/4/22 23:59
 */
public class LongEventMain {

    public static void main(String[] args) throws Exception {
        // The factory for the event
        LongEventFactory factory = new LongEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        // 当仅有一个producer时显示指定ProducerType为single类型，从而可大大提高性能；默认为MULTI
        // BusySpinWaitStrategy： 最高性能，但需要确保handler线程数小于物理核心数
        // YieldingWaitStrategy: 适用于低延迟的场景，100%的利用CPU资源
        Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE, new BlockingWaitStrategy());
        CountDownLatch shutdownLatch = new CountDownLatch(1);

        // Connect the handler
        // 多个EventHandler并行执行(每个handler均会处理按需消费每个消息)，如果需要顺序执行，则通过then函数实现
        disruptor.handleEventsWith(new LongEventHandler(shutdownLatch), (event, sequence, endOfBatch) -> {
            System.out.println("handler2(" + Thread.currentThread().getId() + "), Event: " + event + " with value: " + event.getValue());
        }).then((event, sequence, endOfBatch) -> {
            Thread.sleep(1000);
            System.out.println("handler3(" + Thread.currentThread().getId() + "), Event: " + event + " with value: " + event.getValue());
        }).then((event, sequence, endOfBatch) -> {
            // 通常最后一个handler可进行资源释放、清理等
            event.clear();
        });

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        // 发布事件
        LongEventProducer producer = new LongEventProducer(ringBuffer);
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; l < 10; l++) {
            bb.putLong(0, l);
            producer.publishEvent(bb);
            Thread.sleep(1000);
        }

        // 通过另外一个生产者发布消息
        LongEventProducerWithTranslator producerWithTranslator = new LongEventProducerWithTranslator(ringBuffer);
        for (long l = 0; l < 10; l++) {
            bb.putLong(0, l);
            producerWithTranslator.publishEventOneArg(bb);
            Thread.sleep(1000);
        }

        disruptor.shutdown(10, TimeUnit.SECONDS);
        shutdownLatch.await();
    }


}
