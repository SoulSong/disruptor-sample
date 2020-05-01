package com.shf.disruptor.single.producer;

/**
 * description :
 * 定义一个事件
 *
 * @author songhaifeng
 * @date 2020/4/22 23:12
 */
public class LongEvent {

    private long value;

    public void set(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    /**
     * 应用于事件被处理完后清空对应的资源占用
     */
    public void clear() {
        // set attributes null
        System.out.println("clear");
    }
}
