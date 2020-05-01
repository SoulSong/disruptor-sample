package com.shf.disruptor.single.producer;

import com.lmax.disruptor.EventFactory;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/4/22 23:35
 */
public class LongEventFactory implements EventFactory<LongEvent> {
    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}