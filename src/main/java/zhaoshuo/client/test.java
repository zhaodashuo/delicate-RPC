package zhaoshuo.client;

import zhaoshuo.client.client.RPCFuture;
import zhaoshuo.client.client.RpcClient;
import zhaoshuo.client.client.ServiceDiscovery;
import zhaoshuo.client.client.proxy.ObjectProxy;
import zhaoshuo.common.protocol.RpcResponse;
import zhaoshuo.testapi.HelloWorld;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-04-23 22:34
 */
public class test {
    public static void main(String[] args) throws InterruptedException {
        String registryAddress="114.215.179.51:2181";
        ServiceDiscovery discovery = new ServiceDiscovery(registryAddress);
        RpcClient rpcClient = new RpcClient(discovery);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        AtomicInteger atomicInteger1 = new AtomicInteger(0);
        int threadNum = 10;
        final int requestNum = 10000;
        Thread[] threads = new Thread[threadNum];
        CountDownLatch countDownLatch = new CountDownLatch(1000);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < requestNum; i++) {
                        try {
                            ObjectProxy client = rpcClient.createAsync(HelloWorld.class);
                            RPCFuture helloFuture = client.call("hello", " zhaoshuo");
                            RpcResponse response = (RpcResponse) helloFuture.get(3000, TimeUnit.MILLISECONDS);
                           // System.out.println(response);
                            if (response.isError()) {
                                System.out.println(response.getError());
                            } else {
                                Object result = response.getResult();
                                if (result.equals("Hello zhaoshuo")) {
                                    System.out.println("error = " + result + "================");
                                    atomicInteger.getAndIncrement();
                                    //        atomicInteger.incrementAndGet();
                                }else{
                                    System.out.println("right =");
                                    atomicInteger1.getAndIncrement();
                                }
                            }
                        }catch (Exception e) {
                            System.out.println("空指针异常");
                        }

                    }
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        //  countDownLatch.await();
      //  System.out.println(atomicInteger.get()+"========================");
        long timeCost = (System.currentTimeMillis() - startTime);
        System.out.println(atomicInteger.get());
        System.out.println(atomicInteger1.get());
        String msg = String.format("Async call total-time-cost:%sms, qps/s=%s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);
        rpcClient.stop();
    }
}


