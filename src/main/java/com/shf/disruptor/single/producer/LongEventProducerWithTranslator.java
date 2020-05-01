package com.shf.disruptor.single.producer;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * description :
 * 通过translator简化事件定义与发布
 *
 * @author songhaifeng
 * @date 2020/4/22 23:46
 */
public class LongEventProducerWithTranslator {
    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducerWithTranslator(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<LongEvent, ByteBuffer> TRANSLATOR =
            (event, sequence, bb) -> event.set(bb.getLong(0));

    public void publishEventOneArg(ByteBuffer bb) {
        ringBuffer.publishEvent(TRANSLATOR, bb);
    }
}
