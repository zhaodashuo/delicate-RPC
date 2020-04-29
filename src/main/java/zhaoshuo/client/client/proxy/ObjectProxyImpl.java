package zhaoshuo.client.client.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zhaoshuo.client.client.ConnectManage;
import zhaoshuo.client.client.RPCFuture;
import zhaoshuo.client.client.RpcClientHandler;
import zhaoshuo.common.protocol.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-04-22 15:23
 */
public class ObjectProxyImpl<T> implements ObjectProxy ,InvocationHandler {
    private static Logger logger=LoggerFactory.getLogger(ObjectProxyImpl.class);
    Class<T> clazz;
    public ObjectProxyImpl(Class<T> interfaceClass){
        this.clazz=interfaceClass;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws ExecutionException, InterruptedException {
        RpcRequest rpcRequest = new RpcRequest();
        //进行参数的设置
            UUID uuid = UUID.randomUUID();
            rpcRequest.setRequestId(uuid.toString());
            rpcRequest.setClassName(method.getDeclaringClass().getName());
            rpcRequest.setMethodName(method.getName());
            rpcRequest.setParameters(args);
            rpcRequest.setParameterTypes(method.getParameterTypes());

        ConnectManage instance = ConnectManage.getInstance();
        RpcClientHandler handler = instance.chooseHandler();
        long time=System.currentTimeMillis();
        RPCFuture rpcFuture = handler.sendRequest(rpcRequest);
        logger.debug("time: {}ms", System.currentTimeMillis() - time);
        return rpcFuture.get();
    }

    @Override
    public RPCFuture call(String funcName, Object... args) {
        ConnectManage connectManage = ConnectManage.getInstance();
        RpcClientHandler handler = connectManage.chooseHandler();
        RpcRequest rpcRequest = createRpcRequest(funcName, args);
        RPCFuture rpcFuture = handler.sendRequest(rpcRequest);
        return rpcFuture;
    }
    private RpcRequest createRpcRequest(String funName,Object... args){
        RpcRequest rpcRequest = new RpcRequest();
        //进行参数的设置
        UUID uuid = UUID.randomUUID();
        rpcRequest.setRequestId(uuid.toString());
        rpcRequest.setClassName(clazz.getName());
        rpcRequest.setMethodName(funName);
        logger.debug(clazz.getName());
        logger.debug(funName);
        rpcRequest.setParameters(args);
        Class<?>[] parameterTypes = new Class<?>[args.length];

        for(int i=0;i<args.length;i++){
            parameterTypes[i]=getClassType(args[i]);
        }
        rpcRequest.setParameterTypes(parameterTypes);
        return rpcRequest;
    }
    private Class<?> getClassType(Object obj){
        Class<?> aClass = obj.getClass();
        String typeName = aClass.getName();
        switch(typeName){
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
        }
        return aClass;


    }

}
