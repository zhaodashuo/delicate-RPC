package zhaoshuo.client.client;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import zhaoshuo.common.protocol.RpcDecoder;
import zhaoshuo.common.protocol.RpcEncoder;
import zhaoshuo.common.protocol.RpcRequest;
import zhaoshuo.common.protocol.RpcResponse;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-02-24 13:12
 *
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new RpcEncoder(RpcRequest.class));//outbound事件
        //解决粘包和拆包问题
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        //解码之后进行反序列化
        cp.addLast(new RpcDecoder(RpcResponse.class));
        //业务处理
        cp.addLast(new RpcClientHandler());
    }

    //客户端和服务端的处理顺序没关系，只要两边的InBound和OutBound处理顺序对就行。
    // InBound和OutBound链的内部处理顺序不能错。
}
