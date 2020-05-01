package com.shf.disruptor.single.producer;

import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * description :
 * 发布事件
 *
 * @author songhaifeng
 * @date 2020/4/22 23:58
 */
public class LongEventProducer {
    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void publishEvent(ByteBuffer bb) {
        // 获取下一个序列
        long sequence = ringBuffer.next();
        try {
            // 根据序列获取ringbuffer中对应的entry
            LongEvent event = ringBuffer.get(sequence);
            // 赋值
            event.set(bb.getLong(0));
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
