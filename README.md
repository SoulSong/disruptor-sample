# Introduction
Disruptor 采用了"阶段写入"的方法。在写入数据之前，先加锁申请批量的空闲存储单元，
之后往队列中写入数据的操作就不需要加锁了，写入的性能因此就提高了。Disruptor 对消费
过程的改造，跟对生产过程的改造是类似的。它先加锁申请批量的可读取的存储单元，之后从队
列中读取数据的操作也就不需要加锁了，读取的性能因此也就提高了。