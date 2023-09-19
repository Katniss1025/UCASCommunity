package com.nowcoder.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTests {
    public static void main(String[] args) {
        BlockingQueue queue = new ArrayBlockingQueue(10);
        Thread producer = new Thread(new Procuder(queue));
        Thread consumer1 = new Thread(new Consumer(queue));
        Thread consumer2 = new Thread(new Consumer(queue));
        Thread consumer3 = new Thread(new Consumer(queue));

        producer.start();
        consumer1.start();
        consumer2.start();
        consumer3.start();
    }
}

class Procuder implements Runnable{
    private BlockingQueue<Integer> queue;
    public Procuder(BlockingQueue<Integer> queue){
        this.queue = queue;
    }
    @Override
    public void run() {
        try {
            for(int i = 0; i<100; i++){
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName()+"生产"+queue.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Consumer implements Runnable {
    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName() + "消费了，消费后还剩" + queue.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
