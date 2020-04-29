package zhaoshuo.client.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zhaoshuo.common.protocol.RpcRequest;
import zhaoshuo.common.protocol.RpcResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;


/**
 * RpcFuture对象和Request对象是一一对应的
 */
public class RPCFuture implements Future<Object> {
    private static final Logger logger = LoggerFactory.getLogger(RPCFuture.class);

    private Sync sync;
    private RpcRequest request;
    private RpcResponse response;
    private long startTime;
    private long responseTimeThreshold = 50000;

    private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<AsyncRPCCallback>();
    private ReentrantLock lock = new ReentrantLock();

    public RPCFuture(RpcRequest request) {
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }



    @Override
    public boolean isDone() {
        return sync.isDone();
    }
    @Override
    public Object get() throws InterruptedException, ExecutionException {
        //方法阻塞直到获取response
        sync.acquire(-1);
        if (this.response != null) {
          //  return this.response;
            return this.response.getResult();
        } else {
            logger.error("返回的结果为null");
            throw new RuntimeException();
        }
    }
//规定时间内接受响应，
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));//在规定的时间内没有获取锁返回false；
        if (success) {
            if (this.response != null) {
                return this.response;
            } else {
                logger.debug("空指针出现,返回结果为null");
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }
    //aqs同步器。state变量的状态和结果时候
    public void done(RpcResponse reponse) {
        this.response = reponse;
        System.out.println(response);
        sync.release(1);//持有锁的话释放
        invokeCallbacks();
        // Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            logger.warn("Service response time is too slow. Request id = " + reponse.getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }
    //响应结果回来才进行处理，否则就把结果扔到集合中待处理。
    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRPCCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }
//这两个方法存在对共享资源的操作，加锁保证线程安全
    public RPCFuture addCallback(AsyncRPCCallback callback) {
        lock.lock();
        try {
            if (isDone()) {//涉及到回调方法的执行和回调方法的添加。
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);//添加到集合保存起来
            }
        } finally {
            lock.unlock();
        }
        return this;
    }
    //对调用结果进行异步处理
    private void runCallback(final AsyncRPCCallback callback) {
        final RpcResponse res = this.response;
        RpcClient.submit(new Runnable() {
            @Override
            public void run() {
                if (!res.isError()) {
                    callback.success(res.getResult());
                } else {
                    callback.fail(new RuntimeException("Response error", new Throwable(res.getError())));
                }
            }
        });
    }

    static class Sync extends AbstractQueuedSynchronizer {//同步器

        private static final long serialVersionUID = 1L;

        //future status
        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;//资源的状态为1代表可以申请成功
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {//比较并交换。预期值和实际内存值相同时，进行更新
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        public boolean isDone() {
            getState();
            return getState() == done;
        }
    }
}
